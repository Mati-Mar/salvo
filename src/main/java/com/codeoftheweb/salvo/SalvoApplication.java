package com.codeoftheweb.salvo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.util.Arrays;

@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}


	@Bean
	public CommandLineRunner initData(PlayerRepository playerRepository, GameRepository gameRepository, GamePlayerRepository gameplayerRepository, ShipRepository shipRepository) {
		return (args) -> {

			Player player1 = new Player("j.bauer@ctu.gov");
			Player player2 = new Player("c.obrian@ctu.gov");
			Player player3 = new Player("t.almeida@ctu.gov");
			Player player4 = new Player("d.palmer@whitehouse.gov");

			playerRepository.save(player1);
			playerRepository.save(player2);
			playerRepository.save(player3);
			playerRepository.save(player4);

			Game game1 = new Game(LocalDateTime.now());
			Game game2 = new Game(LocalDateTime.now().plusHours(1));
			Game game3 = new Game(LocalDateTime.now().plusHours(2));
			Game game4 = new Game(LocalDateTime.now().plusHours(3));
			Game game5 = new Game(LocalDateTime.now().plusHours(4));
			Game game6 = new Game(LocalDateTime.now().plusHours(5));

			gameRepository.save(game1);
			gameRepository.save(game2);
			gameRepository.save(game3);
			gameRepository.save(game4);
			gameRepository.save(game5);
			gameRepository.save(game6);

			GamePlayer gameplayer1 = new GamePlayer(game1, player1, LocalDateTime.now());
			GamePlayer gameplayer2 = new GamePlayer(game1, player2, LocalDateTime.now());

			GamePlayer gameplayer3 = new GamePlayer(game2, player1, LocalDateTime.now().plusHours(1));
			GamePlayer gameplayer4 = new GamePlayer(game2, player2, LocalDateTime.now().plusHours(1));

			GamePlayer gameplayer5 = new GamePlayer(game3, player2, LocalDateTime.now().plusHours(2));
			GamePlayer gameplayer6 = new GamePlayer(game3, player3, LocalDateTime.now().plusHours(2));

			GamePlayer gameplayer7 = new GamePlayer(game4, player1, LocalDateTime.now().plusHours(3));
			GamePlayer gameplayer8 = new GamePlayer(game4, player2, LocalDateTime.now().plusHours(3));

			GamePlayer gameplayer9 = new GamePlayer(game5, player3, LocalDateTime.now().plusHours(4));
			GamePlayer gameplayer10 = new GamePlayer(game5, player1, LocalDateTime.now().plusHours(4));

			GamePlayer gameplayer11 = new GamePlayer(game6, player4, LocalDateTime.now().plusHours(5));

			gameplayerRepository.save(gameplayer1);
			gameplayerRepository.save(gameplayer2);
			gameplayerRepository.save(gameplayer3);
			gameplayerRepository.save(gameplayer4);
			gameplayerRepository.save(gameplayer5);
			gameplayerRepository.save(gameplayer6);
			gameplayerRepository.save(gameplayer7);
			gameplayerRepository.save(gameplayer8);
			gameplayerRepository.save(gameplayer9);
			gameplayerRepository.save(gameplayer10);
			gameplayerRepository.save(gameplayer11);

			Ship ship1 = new Ship("Destroyer", gameplayer1, Arrays.asList("H1","H2"));
			Ship ship2 = new Ship("Cruiser", gameplayer1, Arrays.asList("H1","H2","H3"));



			shipRepository.save(ship1);
			shipRepository.save(ship2);


		};
	}

}
