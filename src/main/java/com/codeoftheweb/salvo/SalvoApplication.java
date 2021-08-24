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
	public CommandLineRunner initData(PlayerRepository playerRepository,
									  GameRepository gameRepository,
									  GamePlayerRepository gameplayerRepository,
									  ShipRepository shipRepository,
									  SalvoRepository salvoRepository,
									  ScoreRepository scoreRepository) {
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

			GamePlayer gamePlayer1 = new GamePlayer(game1, player1, LocalDateTime.now());
			GamePlayer gamePlayer2 = new GamePlayer(game1, player2, LocalDateTime.now());

			GamePlayer gamePlayer3 = new GamePlayer(game2, player1, LocalDateTime.now().plusHours(1));
			GamePlayer gamePlayer4 = new GamePlayer(game2, player2, LocalDateTime.now().plusHours(1));

			GamePlayer gamePlayer5 = new GamePlayer(game3, player2, LocalDateTime.now().plusHours(2));
			GamePlayer gamePlayer6 = new GamePlayer(game3, player3, LocalDateTime.now().plusHours(2));

			GamePlayer gamePlayer7 = new GamePlayer(game4, player1, LocalDateTime.now().plusHours(3));
			GamePlayer gamePlayer8 = new GamePlayer(game4, player2, LocalDateTime.now().plusHours(3));

			GamePlayer gamePlayer9 = new GamePlayer(game5, player3, LocalDateTime.now().plusHours(4));
			GamePlayer gamePlayer10 = new GamePlayer(game5, player1, LocalDateTime.now().plusHours(4));

			GamePlayer gamePlayer11 = new GamePlayer(game6, player4, LocalDateTime.now().plusHours(5));

			gameplayerRepository.save(gamePlayer1);
			gameplayerRepository.save(gamePlayer2);
			gameplayerRepository.save(gamePlayer3);
			gameplayerRepository.save(gamePlayer4);
			gameplayerRepository.save(gamePlayer5);
			gameplayerRepository.save(gamePlayer6);
			gameplayerRepository.save(gamePlayer7);
			gameplayerRepository.save(gamePlayer8);
			gameplayerRepository.save(gamePlayer9);
			gameplayerRepository.save(gamePlayer10);
			gameplayerRepository.save(gamePlayer11);

			Ship ship1 = new Ship("Destroyer", gamePlayer1, Arrays.asList("H1","H2","H3"));
			Ship ship2 = new Ship("Carrier", gamePlayer1, Arrays.asList("G1", "G2", "G3", "G4", "G5"));

			Ship ship3 = new Ship("Submarine", gamePlayer2, Arrays.asList("A2","A3", "A4"));
			Ship ship4 = new Ship("Patrol Boat", gamePlayer2, Arrays.asList("G1","G2"));

			Ship ship5 = new Ship("Battleship", gamePlayer3, Arrays.asList("B3" ,"B4", "B5", "B6"));
			Ship ship6 = new Ship("Carrier", gamePlayer3, Arrays.asList("C1", "C2", "C3", "C4", "C5"));

			shipRepository.save(ship1);
			shipRepository.save(ship2);
			shipRepository.save(ship3);
			shipRepository.save(ship4);
			shipRepository.save(ship5);
			shipRepository.save(ship6);

			Salvo salvo1 = new Salvo(1,  gamePlayer1, Arrays.asList("H1","H2"));
			Salvo salvo2 = new Salvo(2,  gamePlayer1, Arrays.asList("A1","A2"));

			Salvo salvo3 = new Salvo(1,  gamePlayer2, Arrays.asList("H1","H2"));
			Salvo salvo4 = new Salvo(2,  gamePlayer2, Arrays.asList("B1","B2", "B3", "B4"));

			Salvo salvo5 = new Salvo(1,  gamePlayer3, Arrays.asList("C1","C2"));
			Salvo salvo6 = new Salvo(2,  gamePlayer3, Arrays.asList("H1","H2", "H3", "H4"));

			salvoRepository.save(salvo1);
			salvoRepository.save(salvo2);
			salvoRepository.save(salvo3);
			salvoRepository.save(salvo4);
			salvoRepository.save(salvo5);
			salvoRepository.save(salvo6);

			Score score1 = new Score( 0d, LocalDateTime.now(), game1, player1);
			Score score2 = new Score( 1d, LocalDateTime.now(), game1, player2);

			Score score3 = new Score( 0.5d, LocalDateTime.now(), game2, player1);
			Score score4 = new Score( 0.5d, LocalDateTime.now(), game2, player2);

			scoreRepository.save(score1);
			scoreRepository.save(score2);

			scoreRepository.save(score3);
			scoreRepository.save(score4);

	};

}
}
