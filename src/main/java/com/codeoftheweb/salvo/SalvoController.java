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

    @Autowired
    private SalvoRepository salvoRepository;

    @Autowired
    private ScoreRepository scoreRepository;
    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }

    @RequestMapping("/games")
    public Map<String, Object> getControllerDTO(Authentication authentication) {
        Map<String, Object> dto = new LinkedHashMap<>();

        if (isGuest(authentication)) {
            dto.put("player", "Guest");
        } else {
            dto.put("player", playerRepository.findByUserName(authentication.getName()).makePlayerDTO());
        }
        dto.put("games", gameRepository.findAll()
                .stream()
                .map(game -> game.makeGameDTO())
                .collect(Collectors.toList()));

        return dto;
    }

    @PostMapping("/games")
    public ResponseEntity<Map<String, Object>> createGame(Authentication authentication) {

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
    public ResponseEntity<Map<String, Object>> joinGame(@PathVariable Long nn,
                                                        Authentication authentication) {

        if (isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("error", "No name"), HttpStatus.UNAUTHORIZED);
        } else {
            if (!gameRepository.findById(nn).isPresent()) {
                return new ResponseEntity<>(makeMap("error", "No such game"), HttpStatus.FORBIDDEN);
            } else {
                if (gameRepository.findById(nn).get().getGamePlayers().size() > 1) {
                    return new ResponseEntity<>(makeMap("error", "Game is full"), HttpStatus.FORBIDDEN);
                } else {
                    if (gameRepository.findById(nn).get().getGamePlayers().stream().findFirst().get().getPlayer().getId() == playerRepository.findByUserName(authentication.getName()).getId()) {
                        return new ResponseEntity<>(makeMap("error", "You're already playing this game"), HttpStatus.FORBIDDEN);
                    } else {
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
    public ResponseEntity<Map<String, Object>> createUser(@RequestParam String email,
                                                          @RequestParam String password) {
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
    public ResponseEntity<Map<String,Object>> findGamePlayer(@PathVariable Long nn, Authentication authentication) {
        Optional<GamePlayer> gamePlayer = gamePlayerRepository.findById(nn);
        if (gamePlayer.isEmpty()) {
            return new ResponseEntity<>(makeMap("error", "No existe el gamePlayer"), HttpStatus.UNAUTHORIZED);
        } else {
            if (gamePlayer.get().getPlayer().getId() != playerRepository.findByUserName(authentication.getName()).getId()) {
                return new ResponseEntity<>(makeMap("error", "No hacer trampa, programador pilluelo"), HttpStatus.UNAUTHORIZED);
            } else {
                if (gamePlayer.get().gameState().equals("WON")) {
                    Score score = new Score(1D, LocalDateTime.now(), gamePlayer.get().getGame(), gamePlayer.get().getPlayer());
                    scoreRepository.save(score);
                } else if (gamePlayer.get().gameState().equals("TIE")) {
                    Score score = new Score(0.5D, LocalDateTime.now(), gamePlayer.get().getGame(), gamePlayer.get().getPlayer());
                    scoreRepository.save(score);
                } else if (gamePlayer.get().gameState().equals("LOST")) {
                    Score score = new Score(0D, LocalDateTime.now(), gamePlayer.get().getGame(), gamePlayer.get().getPlayer());
                    scoreRepository.save(score);
                }
                return new ResponseEntity<>(gamePlayer.get().makeGameViewDTO(), HttpStatus.ACCEPTED);
            }
        }
    }


        @PostMapping("/games/players/{nn}/ships")
    public ResponseEntity<Map<String, Object>> placeShips(Authentication authentication,
                                                          @PathVariable Long nn,
                                                          @RequestBody List<Ship> ships) {

        Optional<GamePlayer> gamePlayer = gamePlayerRepository.findById(nn);

        if (isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("error", "No estás logueado. Iniciá sesión"), HttpStatus.UNAUTHORIZED);
        }

        if (gamePlayer.isEmpty()) {
            return new ResponseEntity<>(makeMap("error", "No existe ese GamePlayer"), HttpStatus.UNAUTHORIZED);
        }

        if (playerRepository.findByUserName(authentication.getName()).getGamePlayers().stream()
                .noneMatch(gp -> gp.equals(gamePlayer.get()))) {
            return new ResponseEntity<>(makeMap("error", "Ese GamePlayer no le corresponde"), HttpStatus.UNAUTHORIZED);
        }

        if (ships.size() != 5) {
            return new ResponseEntity<>(makeMap("error", "Son 5 barcos"), HttpStatus.FORBIDDEN);
        }

        if (gamePlayer.get().getShips().size() != 0) {
            return new ResponseEntity<>(makeMap("error", "Ya colocaste todos los barcos"), HttpStatus.FORBIDDEN);
        }

        for (Ship newShip : ships) {

            if (newShip.getType().equals("destroyer") && newShip.getShipLocations().size() != 3) {
                return new ResponseEntity<>(makeMap("error", "El tamaño del Destroyer es de 3 cuadros"), HttpStatus.FORBIDDEN);
            }

            if (newShip.getType().equals("patrolboat") && newShip.getShipLocations().size() != 2) {
                return new ResponseEntity<>(makeMap("error", "El tamaño del Patrol boat es de 2 cuadros"), HttpStatus.FORBIDDEN);
            }

            if (newShip.getType().equals("submarine") && newShip.getShipLocations().size() != 3) {
                return new ResponseEntity<>(makeMap("error", "El tamaño del Submarine es de 3 cuadros"), HttpStatus.FORBIDDEN);
            }

            if (newShip.getType().equals("battleship") && newShip.getShipLocations().size() != 4) {
                return new ResponseEntity<>(makeMap("error", "El tamaño del Battleship es de 4 cuadros"), HttpStatus.FORBIDDEN);
            }

            if (newShip.getType().equals("carrier") && newShip.getShipLocations().size() != 5) {
                return new ResponseEntity<>(makeMap("error", "El tamaño del Carrier es de 5 cuadros"), HttpStatus.FORBIDDEN);
            }
        }

        for (Ship newShip : ships) {
            newShip.setGamePlayer(gamePlayer.get());
            shipRepository.save(newShip);
        }

        return new ResponseEntity<>(makeMap("OK", "Colocó todo correctamente!"), HttpStatus.ACCEPTED);
    }

    @GetMapping("/games/players/{nn}/ships")
    public ResponseEntity<Map<String, Object>> placeShips(Authentication authentication,
                                                          @PathVariable Long nn) {

        Optional<GamePlayer> gamePlayer = gamePlayerRepository.findById(nn);

        if (isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("error", "No estás logueado. Iniciá sesión"), HttpStatus.UNAUTHORIZED);
        }

        if (gamePlayer.isEmpty()) {
            return new ResponseEntity<>(makeMap("error", "No existe ese GamePlayer"), HttpStatus.UNAUTHORIZED);
        }

        if (playerRepository.findByUserName(authentication.getName()).getGamePlayers().stream()
                .noneMatch(gp -> gp.equals(gamePlayer.get()))) {
            return new ResponseEntity<>(makeMap("error", "Ese GamePlayer no le corresponde"), HttpStatus.UNAUTHORIZED);
        }

        if (gamePlayer.get().getShips().size() == 0) {
            return new ResponseEntity<>(makeMap("error", "No hay barcos"), HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(makeMap("ships", gamePlayer.get().getShips()
                .stream()
                .map(Ship::makeShipDTO)
                .collect(Collectors.toList())), HttpStatus.ACCEPTED);
    }

    @PostMapping("/games/players/{nn}/salvoes")
    public ResponseEntity<Map<String, Object>> storeSalvoes(Authentication authentication,
                                                            @RequestBody Salvo salvoes,
                                                            @PathVariable Long nn) {

        Optional<GamePlayer> gamePlayer1 = gamePlayerRepository.findById(nn);

        Optional<GamePlayer> gamePlayer2 = gamePlayer1.get().getGame().getGamePlayers().stream().filter(gp -> gp != gamePlayer1.get()).findFirst();

        if (isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("error", "No estás logueado. Iniciá sesión"), HttpStatus.UNAUTHORIZED);
        }

        if (gamePlayer1.isEmpty()  || gamePlayer2.isEmpty()) {
            return new ResponseEntity<>(makeMap("error", "El gamePlayer está vacío"), HttpStatus.UNAUTHORIZED);
        }

        if (playerRepository.findByUserName(authentication.getName()).getGamePlayers().stream()
                .noneMatch(gp -> gp.equals(gamePlayer1.get()))) {
            return new ResponseEntity<>(makeMap("error", "Ese GamePlayer no le corresponde"), HttpStatus.UNAUTHORIZED);
        }

        if ((gamePlayer1.get().getSalvos().size() - gamePlayer2.get().getSalvos().size()) > 0  ) {
            return new ResponseEntity<>(makeMap("error", "Espere al oponente"), HttpStatus.FORBIDDEN);
        }

        //Si todavia no hay barcos puestos no puede tirar
        if (gamePlayer1.get().getShips().size() != 5) {
            return new ResponseEntity<>(makeMap("error", "No colocó los barcos"), HttpStatus.UNAUTHORIZED);
        }

        if (gamePlayer2.get().getShips().size() != 5) {
            return new ResponseEntity<>(makeMap("error", "Espere a que el otro jugador coloque los barcos"), HttpStatus.UNAUTHORIZED);
        }

        //Si la lista es menor a 1 y mayor a 5 tiene que dar error
        if (salvoes.getSalvoLocations().size() < 1 || salvoes.getSalvoLocations().size() > 5) {
            return new ResponseEntity<>(makeMap("error", "Se tienen que realizar entre 1 a 5 tiros"), HttpStatus.FORBIDDEN);
        }

        salvoRepository.save(new Salvo(gamePlayer1.get().getSalvos().size() + 1, gamePlayer1.get(), salvoes.getSalvoLocations()));

        return new ResponseEntity<>(makeMap("OK", "Hizo los tiros correctamente!"), HttpStatus.CREATED);
    }

    @GetMapping("/games/players/{nn}/salvoes")
    public ResponseEntity<Map<String, Object>> storeSalvoes(Authentication authentication,
                                                            @PathVariable Long nn) {

        Optional<GamePlayer> gamePlayer = gamePlayerRepository.findById(nn);

        if (isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("error", "No estás logueado. Iniciá sesión"), HttpStatus.UNAUTHORIZED);
        }

        if (gamePlayer.isEmpty()) {
            return new ResponseEntity<>(makeMap("error", "No existe ese GamePlayer"), HttpStatus.UNAUTHORIZED);
        }

        if (playerRepository.findByUserName(authentication.getName()).getGamePlayers().stream()
                .noneMatch(gp -> gp.equals(gamePlayer.get()))) {
            return new ResponseEntity<>(makeMap("error", "Ese GamePlayer no le corresponde"), HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(makeMap("salvoes", gamePlayer.get().getSalvos()
                .stream()
                .map(Salvo::makeSalvoDTO)
                .collect(Collectors.toList())), HttpStatus.ACCEPTED);

        }
}