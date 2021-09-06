package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private ShipRepository shipRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

    @RequestMapping("/games")
    public Map<String, Object> getControllerDTO(Authentication authentication) {
        Map <String, Object> dto = new LinkedHashMap<>();

        if (isGuest(authentication)) {
            dto.put("player","Guest");
        }
        else
        {
            dto.put("player",playerRepository.findByUserName(authentication.getName()).makePlayerDTO());
        }
        dto.put("games", gameRepository.findAll()
                .stream()
                .map(game -> game.makeGameDTO())
                .collect(Collectors.toList()));

        return dto;
    }

    @PostMapping ("/games")
    public ResponseEntity<Map<String, Object>> createGame(Authentication authentication){

        if (isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("error", "Inicie sesión para crear un juego"), HttpStatus.UNAUTHORIZED);
        } else {

            Game newGame = gameRepository.save(new Game(LocalDateTime.now()));
            gameRepository.save(newGame);

            Player currentPlayer = playerRepository.findByUserName(authentication.getName());
            GamePlayer newGamePlayer = new GamePlayer(newGame, currentPlayer, LocalDateTime.now());
            gamePlayerRepository.save(newGamePlayer);

            return new ResponseEntity<>(makeMap("gpid", newGamePlayer.getId()), HttpStatus.CREATED);
        }
    }

    @PostMapping("/game/{nn}/players")
    public ResponseEntity<Map<String, Object>> joinGame(@PathVariable Long nn,Authentication authentication) {

        if (isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("error", "No name"), HttpStatus.UNAUTHORIZED );
        } else {
            if (!gameRepository.findById(nn).isPresent()) {
                return new ResponseEntity<>(makeMap("error", "No such game"), HttpStatus.FORBIDDEN);
            } else {
                if (gameRepository.findById(nn).get().getGamePlayers().size() > 1) {
                    return new ResponseEntity<>(makeMap("error", "Game is full"), HttpStatus.FORBIDDEN);
                }
                else {
                    if (gameRepository.findById(nn).get().getGamePlayers().stream().findFirst().get().getPlayer().getId() == playerRepository.findByUserName(authentication.getName()).getId()) {
                        return new ResponseEntity<>(makeMap("error", "You're already playing this game"), HttpStatus.FORBIDDEN);
                    }
                    else {
                        Player currentPlayer = playerRepository.findByUserName(authentication.getName());
                        Game joinedGame = gameRepository.findById(nn).get();

                        GamePlayer newGamePlayer = new GamePlayer(joinedGame, currentPlayer, LocalDateTime.now());
                        gamePlayerRepository.save(newGamePlayer);

                        return new ResponseEntity<>(makeMap("gpid", newGamePlayer.getId()), HttpStatus.CREATED);
                    }
                }
            }
        }
    }

    @PostMapping("/players")
        public ResponseEntity<Map<String, Object>> createUser(@RequestParam String email, @RequestParam String password) {
            if (email.isEmpty()) {
                return new ResponseEntity<>(makeMap("error", "No name"), HttpStatus.UNAUTHORIZED);
            }
            Player player = playerRepository.findByUserName(email);
            if (player != null) {
                return new ResponseEntity<>(makeMap("error", "Username already exists"), HttpStatus.FORBIDDEN);
            }
            Player newPlayer = playerRepository.save(new Player(email, passwordEncoder.encode(password)));
            return new ResponseEntity<>(makeMap("name", newPlayer.getUserName()), HttpStatus.CREATED);
        }

        private Map<String, Object> makeMap(String key, Object value) {
            Map<String, Object> map = new HashMap<>();
            map.put(key, value);
            return map;
        }

    @RequestMapping("/game_view/{nn}")
    public ResponseEntity<Map<String, Object>> findGamePlayer(@PathVariable Long nn, Authentication authentication) {

        Optional<GamePlayer> gamePlayer = gamePlayerRepository.findById(nn);

        if (!gamePlayer.isPresent()) {
            return new ResponseEntity<>(makeMap("error", "No exsite ese gamePlayer"), HttpStatus.UNAUTHORIZED);
        }
        else {
            if (gamePlayer.get().getPlayer().getId() != playerRepository.findByUserName(authentication.getName()).getId()) {
                return new ResponseEntity<>(makeMap("error", "No hagas trampa"), HttpStatus.UNAUTHORIZED);
            }
            else {
                return new ResponseEntity<>(gamePlayer.get().makeGameViewDTO(), HttpStatus.ACCEPTED);
            }
        }
    }
}