package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Entity
public class Ship {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;
    private String type;

    @ElementCollection
    @Column (name="shipLocation")
    private List<String> shipLocation  = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayerID")
    private GamePlayer gamePlayerID;

    public Ship() { }

    public Ship(String type, GamePlayer gamePlayerID, List<String> shipLocation) {
        this.type = type;
        this.gamePlayerID = gamePlayerID;
        this.shipLocation = shipLocation;
    }

    public Map<String, Object> makeShipDTO() {

        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("type" , this.getType());
        dto.put("location" , this.getShipLocation());

        return dto;
    }

    public GamePlayer getGamePlayerID() {
        return gamePlayerID;
    }

    public void setGamePlayerID(GamePlayer gamePlayerID) {
        this.gamePlayerID = gamePlayerID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getShipLocation() {
        return shipLocation;
    }

    public void setShipLocation(List<String> shipLocation) {
        this.shipLocation = shipLocation;
    }




}
