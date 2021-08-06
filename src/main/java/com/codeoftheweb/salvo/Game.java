package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;


@Entity
public class Game {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;


    @OneToMany(mappedBy = "gameID", fetch = FetchType.EAGER)
    Set<GamePlayer> gamePlayers;

/*    public void addGamePlayer(GamePlayer gameplayer) {
        gamePlayer.setGameID(this);
        gamePlayers.add(gamePlayer);
     } */

    private LocalDateTime creationDate;

    public Game() {
    } /*Siempre constructor vacío*/

    public Game(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public Game(Set<GamePlayer> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public void setGamePlayers(Set<GamePlayer> gameplayers) {
        this.gamePlayers = gameplayers;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }


    @JsonIgnore
    public List<Player> getPlayers() {
        return gamePlayers.stream().map(sub -> sub.getPlayerID()).collect(toList());

    }

}



