package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Entity
public class GamePlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    private LocalDateTime joinDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;

    @OneToMany(mappedBy="gamePlayer", fetch=FetchType.EAGER)
    Set<Ship> ships;

    @OneToMany(mappedBy="gamePlayer", fetch=FetchType.EAGER)
    @OrderBy
    Set<Salvo> salvos;

    private int Hits;

    public GamePlayer() { } /*Siempre constructor vacío*/

    public GamePlayer(Game game, Player player, LocalDateTime joinDate) {
        this.game = game;
        this.player = player;
        this.joinDate = joinDate;
    }


    public LocalDateTime getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDateTime joinDate) {
        this.joinDate = joinDate;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Map<String, Object> makeGamePlayerDTO(){

        Map <String, Object> dto = new LinkedHashMap<>();
        dto.put("id", this.getId());
        dto.put("player",this.getPlayer().makePlayerDTO() );

        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<Ship> getShips() {
        return ships;
    }

    public void setShips(Set<Ship> ships) {
        this.ships = ships;
    }

    public Set<Salvo> getSalvos() {
        return salvos;
    }

    public void setSalvos(Set<Salvo> salvos) {
        this.salvos = salvos;
    }

//    private List<String> self = new ArrayList<>();
//      YA NO ES NECESARIO
//    private List<String> opponent = new ArrayList<>();

    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
    }

    private String gameState() {
        String gameState= "";

        //    WAITINGFOROPP,
        //    WAIT,
        //    PLAY,
        //    PLACESHIPS,
        //    WON,
        //    LOST,
        //    TIE,
        //    UNDEFINED
        GamePlayer opponent = this.getOpponent();

        if (opponent == null) {
            gameState = "WAITINGFOROPP";
        }
        if (this.getShips().size() != 5) {
            gameState = "PLACESHIPS";
        }
        if (opponent.getShips().size() != 5) {
            gameState = "WAIT";
        }

        if (this.getShips().size() == 5 && opponent.getShips().size() == 5) {
            gameState = "PLAY";
        }

        if (this.getSalvos().size() > opponent.getSalvos().size()) {
            gameState = "WAIT";
        }
        else {
            gameState = "PLAY";
        }



        return gameState;
    }

    public GamePlayer getOpponent() {
        GamePlayer opponent = this.getGame().getGamePlayers()
                .stream()
                .filter(gp -> gp.getId() != this.getId())
                .findFirst().orElse(new GamePlayer());
        return opponent;
    }



   public List <Map <String, Object>> hitsBarcos(){
        List <Map <String, Object>> hitsBarcos = new ArrayList<>();


           //Obtengo la posición del barco dependiendo su tipo
           Ship carrier = this.getShips().stream().filter(sh -> sh.getType().equals("carrier")).findFirst().get();
           List<String> carrierLocations = carrier.getShipLocations();

           Ship battleship = this.getShips().stream().filter(sh -> sh.getType().equals("battleship")).findFirst().get();
           List<String> battleshipLocations = battleship.getShipLocations();

           Ship submarine = this.getShips().stream().filter(sh -> sh.getType().equals("submarine")).findFirst().get();
           List<String> submarineLocations = submarine.getShipLocations();

           Ship destroyer = this.getShips().stream().filter(sh -> sh.getType().equals("destroyer")).findFirst().get();
           List<String> destroyerLocations = destroyer.getShipLocations();

           Ship patrolboat = this.getShips().stream().filter(sh -> sh.getType().equals("patrolboat")).findFirst().get();
           List<String> patrolboatLocations = patrolboat.getShipLocations();


        Map <String, Object> damages = new LinkedHashMap<>();

        //Defino los contadores para ver cuantas veces golpeó al barco

       int carrierHitsTotal = 0;
       int battleshipHitsTotal = 0;
       int submarineHitsTotal = 0;
       int destroyerHitsTotal = 0;
       int patrolboatHitsTotal = 0;

        //Se recorre el Salvo de cada turno
        for (Salvo salvoTurn:getOpponent().getSalvos()) {

            Map <String, Object> mapBarcos = new LinkedHashMap<>();



            //List<Salvo> ListaSalvo= getOpponent().getSalvos().stream().filter(gp->gp.getTurn()<=salvoTurn.getTurn()).collect(Collectors.toList());

            int carrierHitsTurno = 0;
            int battleshipHitsTurno = 0;
            int submarineHitsTurno = 0;
            int destroyerHitsTurno = 0;
            int patrolboatHitsTurno = 0;
            int missed = salvoTurn.getSalvoLocations().size();

            List<String> hitsLocations = new ArrayList<>();

            for (String salvoLocation: salvoTurn.getSalvoLocations()) {
                if (carrierLocations.contains(salvoLocation)) {
                    carrierHitsTotal++;
                    carrierHitsTurno++;
                    missed--;
                    hitsLocations.add(salvoLocation);
                }
                if (battleshipLocations.contains(salvoLocation)) {
                    battleshipHitsTotal++;
                    battleshipHitsTurno++;
                    missed--;
                    hitsLocations.add(salvoLocation);
                }
                if (submarineLocations.contains(salvoLocation)) {
                    submarineHitsTotal++;
                    submarineHitsTurno++;
                    missed--;
                    hitsLocations.add(salvoLocation);
                }
                if (destroyerLocations.contains(salvoLocation)) {
                    destroyerHitsTotal++;
                    destroyerHitsTurno++;
                    missed--;
                    hitsLocations.add(salvoLocation);
                }
                if (patrolboatLocations.contains(salvoLocation)) {
                    patrolboatHitsTotal++;
                    patrolboatHitsTurno++;
                    missed--;
                    hitsLocations.add(salvoLocation);
                }


            }
            damages.put("carrierHits", carrierHitsTurno);
            damages.put("battleshipHits", battleshipHitsTurno);
            damages.put("submarineHits", submarineHitsTurno);
            damages.put("destroyerHits", destroyerHitsTurno);
            damages.put("patrolboatHits", patrolboatHitsTurno);

            damages.put("carrier", carrierHitsTotal);
            damages.put("battleship", battleshipHitsTotal);
            damages.put("submarine", submarineHitsTotal);
            damages.put("destroyer", destroyerHitsTotal);
            damages.put("patrolboat", patrolboatHitsTotal);

            mapBarcos.put("turn", salvoTurn.getTurn());
            mapBarcos.put("hitLocations", hitsLocations);
            mapBarcos.put("damages", damages);
            mapBarcos.put("missed", missed);

            hitsBarcos.add(mapBarcos);

        }
        return hitsBarcos;
    }


    public Map<String, Object> makeGameViewDTO(){
        Map<String, Object>     dto= new LinkedHashMap<>();
        dto.put("id", this.getGame().getId());
        dto.put("created", this.getGame().getCreationDate());
        dto.put("gameState" , gameState());  //Recordar de cambiar a  PLACESHIPS ,PLAY, WAIT, LOSE, etc.
        dto.put("gamePlayers", this.getGame().getGamePlayers()
                .stream()
                .map(gamePlayer -> gamePlayer.makeGamePlayerDTO())
                .collect(Collectors.toList()));
        dto.put("ships" ,this.getShips()
                .stream()
                .map(ship -> ship.makeShipDTO())
                .collect(Collectors.toList()));
        dto.put("salvoes" ,this.getGame().getGamePlayers()
                .stream()
                .flatMap(gamePlayer -> gamePlayer.getSalvos()
                        .stream()
                        .map(salvo -> salvo.makeSalvoDTO())
                        )
                .collect(Collectors.toList()));

        GamePlayer opponent = this.getOpponent();

        Map<String, Object> hits = new LinkedHashMap<>();

        if (opponent.getId() == null || opponent.getShips().size() == 0 || this.getShips().size() == 0) {
            hits.put("self", new ArrayList<>());
            hits.put("opponent", new ArrayList<>());
        }
        else {
            hits.put("self", this.hitsBarcos());
            hits.put("opponent", opponent.hitsBarcos());
        }
        dto.put("hits", hits);
         return dto;
    }

    public Optional<Score> getScore() {
        return this.getPlayer().getScore(this.getGame());
    }
}
