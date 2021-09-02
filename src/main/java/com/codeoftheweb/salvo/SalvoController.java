package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    @PostMapping("/players")
        public ResponseEntity<Map<String, Object>> createUser(@RequestParam String email, @RequestParam String password) {
            if (email.isEmpty()) {
                return new ResponseEntity<>(makeMap("error", "No name"), HttpStatus.FORBIDDEN);
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

        GamePlayer gamePlayer = gamePlayerRepository.findById(nn).get();

        if (gamePlayer.getPlayer().getId() != playerRepository.findByUserName(authentication.getName()).getId()) {
            return new ResponseEntity<>(makeMap("error", "No hagas trampa"), HttpStatus.UNAUTHORIZED);
        }
        else {
            return new ResponseEntity<>(gamePlayer.makeGameViewDTO(), HttpStatus.ACCEPTED);
        }


    }

}
