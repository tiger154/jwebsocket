/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.gaming.pingpong.plugin;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import org.jwebsocket.gaming.pingpong.objects.PingpongGame;
import org.jwebsocket.gaming.pingpong.objects.PingpongMatch;
import org.jwebsocket.gaming.pingpong.objects.PingpongPlayer;
import org.jwebsocket.gaming.pingpong.objects.User;
import org.jwebsocket.gaming.pingpong.services.UserServiceImpl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;

/**
 *
 * @author armando
 */
public class PingPongPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger(PingPongPlugIn.class);
	private PingpongGame mPingpongGame;
	private UserServiceImpl mUserServiceImpl;
	private boolean mDatabaseError;
	private String mDatabaseErrorMessage;

	public PingPongPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		setNamespace(aConfiguration.getNamespace());
		// System.out.println(">> Loading the plug-in...");
		if (mLog.isDebugEnabled()) {
			mLog.debug("PlugIn instantiated successfully!");
		}
		try {
			String lHost = (String) aConfiguration.getSettings().get("dbHost");
			String lPort = (String) aConfiguration.getSettings().get("dbPort");
			int lDay = Integer.parseInt(aConfiguration.getSettings().
					get("dbDay").toString());
			int lTime = Integer.parseInt(aConfiguration.getSettings().
					get("dbTime").toString());
			int lWidth = Integer.parseInt(aConfiguration.getSettings().
					get("sWidth").toString());
			int lHeight = Integer.parseInt(aConfiguration.getSettings().
					get("sHeight").toString());
			mPingpongGame = new PingpongGame(lWidth, lHeight, 2);
			Mongo lMongo = new Mongo(lHost + ":" + lPort);
			DBCollection lCollection = lMongo.getDB("pingpongame").
					getCollection("user");
			mUserServiceImpl = new UserServiceImpl(lCollection, lDay);
			Tools.getTimer().scheduleAtFixedRate(mUserServiceImpl, 0,
					lTime * 1000 * 60 * 60 * 24);

		} catch (Exception ex) {
			if (mLog.isDebugEnabled()) {
				mLog.error("There was an error during the connection: " + ex.getMessage());
				mDatabaseError = true;
				mDatabaseErrorMessage = "There was an error during the connection: " + ex.getMessage();
			}
		}
	}

	@Override
	public void connectorStarted(WebSocketConnector aConnector) {
		if (mDatabaseError) {
			Token lToken = TokenFactory.createToken("pingpong", "databaseError");
			lToken.setString("msg", mDatabaseErrorMessage);
			getServer().sendToken(aConnector, lToken);
		}
	}

	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		try {
			if (aToken.getType().equals("stage")) {
				processInitStage(aConnector, aToken);
			} else if (aToken.getType().equals("usser")) {
				processUsser(aConnector, aToken);
			} else if (aToken.getType().equals("sendrequest")) {
				processSendRequest(aConnector, aToken);
			} else if (aToken.getType().equals("submitsequest")) {
				processSubmitRequest(aConnector, aToken);
			} else if (aToken.getType().equals("moveplayer")) {
				processMovePlayer(aConnector, aToken);
			} else if (aToken.getType().equals("newgame")) {
				processNewGame(aConnector, aToken);
			} else if (aToken.getType().equals("pause")) {
				processPause(aConnector, aToken);
			} else if (aToken.getType().equals("sendnewgame")) {
				processSendNewGame(aConnector, aToken);
			} else if (aToken.getType().equals("endgame")) {
				processEndGame(aConnector, aToken);
			} else if (aToken.getType().equals("logoff")) {
				processLogoff(aConnector, aToken);
			} else if (aToken.getType().equals("createaccount")) {
				processCreateAccount(aConnector, aToken);
			} else if (aToken.getType().equals("sms")) {
				processSms(aConnector, aToken);
			} else if (aToken.getType().equals("sound")) {
				processSound(aConnector, aToken);
			}
		} catch (Exception ex) {
			if (mLog.isDebugEnabled()) {
				mLog.error("processToken. " + ex.getMessage());
			}
		}

	}

	@Override
	public void connectorStopped(WebSocketConnector aConnector,
			CloseReason aCloseReason) {
		if (mPingpongGame.getMatch(aConnector.getString("username")) != null) {
			PingpongMatch lPingpongMatch = mPingpongGame.getMatch(
					aConnector.getString("username"));
			lPingpongMatch.destroy();
			for (PingpongPlayer lPlayer : lPingpongMatch.getPingpongPlayerList()) {
				if (!lPlayer.getPlayerName().equals(
						aConnector.getString("username"))) {
					WebSocketConnector lConnector = connectorUser(
							lPlayer.getPlayerName());
					lConnector.setBoolean("state", false);
					lConnector.setString("connected", null);
					mPingpongGame.deleteMatch(lPingpongMatch);
					updatePlayerScore(lPlayer.getPlayerName());
					Token lMessage = TokenFactory.createToken(getNamespace(),
							"sendexit");
					lMessage.setString("username",
							aConnector.getString("username"));
					getServer().sendToken(lConnector, lMessage);
					break;
				}
			}
		} else {
			if (aConnector.getString("connected") != null) {
				Token lMessage = TokenFactory.createToken(getNamespace(),
						"submitsequestno");
				lMessage.setString("username", aConnector.getString("username"));
				getServer().sendToken(connectorUser(
						aConnector.getString("connected")), lMessage);
				WebSocketConnector lConnector = connectorUser(
						aConnector.getString("connected"));
				lConnector.setBoolean("state", false);
				lConnector.setString("connected", null);
			}

		}
		aConnector.setString("username", null);
		aConnector.setBoolean("state", null);
		onlineUser();
	}

	// cuando se elimina una partida 
	public void processEndGame(WebSocketConnector aConnector, Token aToken) {

		PingpongMatch lPingpongMatch = mPingpongGame.getMatch(
				aConnector.getString("username"));
		lPingpongMatch.destroy();
		for (PingpongPlayer lPlayer : lPingpongMatch.getPingpongPlayerList()) {
			WebSocketConnector lConnector = connectorUser(lPlayer.getPlayerName());
			lConnector.setBoolean("state", false);
			updatePlayerScore(lPlayer.getPlayerName());
			if (!lPlayer.getPlayerName().equals(aConnector.getString("username"))) {
				Token lMessage = TokenFactory.createToken(
						getNamespace(), "sendexit");
				lMessage.setString("username",
						aConnector.getString("username"));
				getServer().sendToken(lConnector, lMessage);
			}
		}

		mPingpongGame.deleteMatch(lPingpongMatch);
		onlineUser();

	}

	//inicialiso mi exenario
	public void processInitStage(WebSocketConnector aConnector, Token aToken) {

		int lWidth = mPingpongGame.getPingpongStage().getStageWidth();
		int lHeight = mPingpongGame.getPingpongStage().getStageHeigth();
		int lGameBorder = mPingpongGame.getPingpongStage().getStageBorder();

		Token lMessage = TokenFactory.createToken(getNamespace(), "stage");
		lMessage.setInteger("width", lWidth);
		lMessage.setInteger("height", lHeight);
		lMessage.setInteger("gameBorder", lGameBorder);

		getServer().sendToken(aConnector, lMessage);

	}

	//Crear usser
	public void processCreateAccount(WebSocketConnector aConnector, Token aToken) {

		User lUser = new User(aToken.getString("username"),
				aToken.getString("pwsname"), 0, 0);
		boolean lCreate = mUserServiceImpl.create(lUser.asDocument());

		if (lCreate) {
			Token lMessage = TokenFactory.createToken(getNamespace(), "loggedinfo");
			lMessage.setBoolean("logged", true);
			lMessage.setString("username", aToken.getString("username"));

			getServer().sendToken(aConnector, lMessage);

			aConnector.setString("username", aToken.getString("username"));
			aConnector.setBoolean("state", false);
			aConnector.setBoolean("sound", true);

			onlineUser();
			ranking();
		} else {
			Token lMessage = TokenFactory.createToken(getNamespace(),
					"userincorrect");
			lMessage.setString("message", "User already exists");
			getServer().sendToken(aConnector, lMessage);
		}


	}

	//Registrar usuario
	public void processUsser(WebSocketConnector aConnector, Token aToken) {

		boolean lPwdCorrect = mUserServiceImpl.isPwdCorrect(
				aToken.getString("username"), aToken.getString("pwsname"));
		WebSocketConnector lConnector = connectorUser(
				aToken.getString("username"));

		if (lPwdCorrect && lConnector == null) {
			Token lMessage = TokenFactory.createToken(getNamespace(),
					"loggedinfo");
			lMessage.setString("username",
					aToken.getString("username"));
			getServer().sendToken(aConnector, lMessage);
			aConnector.setString("username",
					aToken.getString("username"));
			aConnector.setBoolean("state", false);
			aConnector.setBoolean("sound", true);
			onlineUser();

			getServer().sendToken(aConnector, orderRanking());
		} else {
			Token lMessage = TokenFactory.createToken(getNamespace(),
					"userincorrect");
			if (lConnector != null) {
				lMessage.setString("message", "You can not be registered more "
						+ "than once with the same user");
			} else {
				lMessage.setString("message", "Verify that you are correct "
						+ "username or password");
			}
			getServer().sendToken(aConnector, lMessage);

		}

	}

	// envio solicitud de juego
	public void processSendRequest(WebSocketConnector aConnector, Token aToken) {

		WebSocketConnector lConnector = connectorUser(aToken.getString("username"));
		lConnector.setBoolean("state", true);
		aConnector.setBoolean("state", true);
		lConnector.setString("connected", aConnector.getString("username"));
		aConnector.setString("connected", lConnector.getString("username"));
		onlineUser();
		Token lMessage = TokenFactory.createToken(getNamespace(), "sendrequest");
		lMessage.setString("username", aConnector.getString("username"));
		getServer().sendToken(lConnector, lMessage);
	}

	//creo la partida com mis jugadores
	public void processSubmitRequest(WebSocketConnector aConnector, Token aToken) {

		boolean lAccepted = aToken.getBoolean("accepted");
		String lUserName1 = aToken.getString("username");
		String lUserName2 = aConnector.getString("username");
		if (lAccepted) {
			mPingpongGame.addMatch(new PingpongMatch(this,
					mPingpongGame.getPingpongStage(), new PingpongPlayer(
					lUserName1, "playLeft", 0, mPingpongGame.getPingpongStage()),
					new PingpongPlayer(lUserName2, "playRight", 0,
					mPingpongGame.getPingpongStage())));
			PingpongMatch lPingpongMatch = mPingpongGame.getMatch(lUserName1);
			playerUpdate(lPingpongMatch, "playLeftplayRight");
			scoreUpdate(lPingpongMatch);
			updateBall(lPingpongMatch);

			Token lMessage = TokenFactory.createToken(getNamespace(), "objarea");
			lMessage.setBoolean("objarea", true);
			getServer().sendToken(connectorUser(
					lPingpongMatch.getPingpongPlayer(0).getPlayerName()),
					lMessage);
			getServer().sendToken(connectorUser(
					lPingpongMatch.getPingpongPlayer(1).getPlayerName()),
					lMessage);
		} else {
			Token lMessage = TokenFactory.createToken(getNamespace(),
					"submitsequestno");
			lMessage.setString("username", lUserName2);
			getServer().sendToken(connectorUser(lUserName1), lMessage);

			WebSocketConnector lConnector = connectorUser(
					aToken.getString("username"));
			lConnector.setBoolean("state", false);
			aConnector.setBoolean("state", false);
			onlineUser();
		}

	}

	// Mando a mover los player
	public void processMovePlayer(WebSocketConnector aConnector, Token aToken) {
		if (aConnector.getBoolean("state")) {
			String lUserName = aConnector.getString("username");
			PingpongMatch lPingpongMatch = mPingpongGame.getMatch(lUserName);
			lPingpongMatch.movePlayer(aToken.getInteger("e"), lUserName,
					aToken.getString("v"));
		}
	}

	//Aqui le envio el msj de que va a comenzar la partida
	public void processSendNewGame(WebSocketConnector aConnector, Token aToken) {

		PingpongMatch lPingpongMatch = mPingpongGame.getMatch(
				aConnector.getString("username"));
		Token lMessage = TokenFactory.createToken(getNamespace(), "sendnewgame");
		if (!lPingpongMatch.stop()) {
			lPingpongMatch.pause();
		}

		for (PingpongPlayer lPlayer : lPingpongMatch.getPingpongPlayerList()) {
			if (!lPlayer.getPlayerName().equals(aConnector.getString("username"))) {
				getServer().sendToken(connectorUser(lPlayer.getPlayerName()),
						lMessage);
				break;
			}
		}
	}

	//Comenzar la partida
	public void processNewGame(WebSocketConnector aConnector, Token aToken) {

		if (aToken.getBoolean("newgame")) {
			mPingpongGame.getMatch(aConnector.getString("username")).NewGame();
		} else {
			if (mPingpongGame.getMatch(aConnector.getString("username")).stop()) {
				mPingpongGame.getMatch(aConnector.getString("username")).pause();
			}
		}
	}

	// Pause o Comtinuo
	public void processPause(WebSocketConnector aConnector, Token aToken) {

		PingpongMatch lPingpongMatch = mPingpongGame.getMatch(
				aConnector.getString("username"));
		if (!lPingpongMatch.stop()) {
			lPingpongMatch.pause();
			Token lMessage = TokenFactory.createToken(getNamespace(), "pause");
			lMessage.setString("pause", "pause");
			getServer().sendToken(connectorUser(
					lPingpongMatch.getPingpongPlayer(0).getPlayerName()),
					lMessage);
			getServer().sendToken(connectorUser(
					lPingpongMatch.getPingpongPlayer(1).getPlayerName()),
					lMessage);
		} else {
			lPingpongMatch.pause();
			Token lMessage = TokenFactory.createToken(getNamespace(), "pause");
			lMessage.setString("pause", "");
			getServer().sendToken(connectorUser(
					lPingpongMatch.getPingpongPlayer(0).getPlayerName()),
					lMessage);
			getServer().sendToken(connectorUser(
					lPingpongMatch.getPingpongPlayer(1).getPlayerName()),
					lMessage);

		}
	}

	//Desconectar al user
	public void processLogoff(WebSocketConnector aConnector, Token aToken) {

		if (mPingpongGame.getMatch(aConnector.getString("username")) != null) {
			PingpongMatch lPingpongMatch = mPingpongGame.getMatch(
					aConnector.getString("username"));
			lPingpongMatch.destroy();
			for (PingpongPlayer lPlayer : lPingpongMatch.getPingpongPlayerList()) {
				WebSocketConnector lConnector = connectorUser(
						lPlayer.getPlayerName());
				lConnector.setBoolean("state", false);
				updatePlayerScore(lPlayer.getPlayerName());
				if (!lPlayer.getPlayerName().equals(
						aConnector.getString("username"))) {
					Token IMessage = TokenFactory.createToken(
							getNamespace(), "sendexit");
					IMessage.setString("username",
							aConnector.getString("username"));
					getServer().sendToken(lConnector, IMessage);
				}
			}

			mPingpongGame.deleteMatch(lPingpongMatch);
		}

		Token lMessage = TokenFactory.createToken(getNamespace(), "usser");
		getServer().sendToken(aConnector, lMessage);
		lMessage = TokenFactory.createToken(getNamespace(), "ranking");
		getServer().sendToken(aConnector, lMessage);
		aConnector.setString("username", null);
		aConnector.setString("state", null);
		onlineUser();
		lMessage = TokenFactory.createToken(getNamespace(), "logoff");
		getServer().sendToken(aConnector, lMessage);

	}

	public void processSms(WebSocketConnector aConnector, Token aToken) {

		PingpongMatch lPingpongMatch = mPingpongGame.getMatch(
				aConnector.getString("username"));
		Token lMessage = TokenFactory.createToken(getNamespace(), "sms");
		lMessage.setString("text", aToken.getString("text"));
		lMessage.setString("username", aConnector.getString("username"));
		if (lPingpongMatch.getPingpongPlayer(0).getPlayerName().equals(
				aConnector.getString("username"))) {
			lMessage.setString("user", "0");
		} else {
			lMessage.setString("user", "1");
		}
		getServer().sendToken(connectorUser(
				lPingpongMatch.getPingpongPlayer(0).getPlayerName()), lMessage);
		getServer().sendToken(connectorUser(
				lPingpongMatch.getPingpongPlayer(1).getPlayerName()), lMessage);
	}

	public void processSound(WebSocketConnector aConnector, Token aToken) {

		if (aConnector.getBoolean("sound")) {
			aConnector.setBoolean("sound", false);
		} else {
			aConnector.setBoolean("sound", true);
		}
	}

	// Aqui le envio la lista para cada conector de los que estan conectados
	private void onlineUser() {
		try {
			Token lMessage = TokenFactory.createToken(getNamespace(), "usser");
			Collection<WebSocketConnector> lCollection = getServer().
					getAllConnectors().values();

			for (WebSocketConnector lConnector : lCollection) {
				if (lConnector.getString("username") != null) {
					List<String> lList1 = new ArrayList<String>();
					List<String> lList2 = new ArrayList<String>();
					for (WebSocketConnector lConnector1 : lCollection) {
						if (!lConnector.equals(lConnector1)
								&& lConnector1.getBoolean("state") != null
								&& !lConnector1.getBoolean("state")) {
							lList1.add(lConnector1.getString("username"));
						} else if (!lConnector.equals(lConnector1)
								&& lConnector1.getBoolean("state") != null
								&& lConnector1.getBoolean("state")) {
							lList2.add(lConnector1.getString("username"));
						}
					}
					lMessage.setBoolean("state", lConnector.getBool("state"));
					lMessage.setList("available", lList1);
					lMessage.setList("playing", lList2);

					getServer().sendToken(lConnector, lMessage);
				}
			}
		} catch (Exception ex) {
			if (mLog.isDebugEnabled()) {
				mLog.error("onlineUser. " + ex.getMessage());
			}
		}
	}

	// Aqui mando a actualizar los player pasandole los nombre los jugadores
	public void playerUpdate(PingpongMatch aPingpongMatch, String aJoystick) {
		try {
			Token lMessage = TokenFactory.createToken(getNamespace(), "submitrequest");
			PingpongPlayer lPingpongPlayer1 = aPingpongMatch.getPingpongPlayer(0);
			PingpongPlayer lPingpongPlayer2 = aPingpongMatch.getPingpongPlayer(1);


			WebSocketConnector lConnector = connectorUser(
					lPingpongPlayer1.getPlayerName());
			WebSocketConnector lConnector1 = connectorUser(
					lPingpongPlayer2.getPlayerName());
			if ("playLeftplayRight".equals(aJoystick)) {
				lMessage.setString("player", "playLeft");
				lMessage.setInteger("width",
						(int) lPingpongPlayer1.getDimension().getWidth());
				lMessage.setInteger("Heigth",
						(int) lPingpongPlayer1.getDimension().getHeight());

				lMessage.setInteger("posX", lPingpongPlayer1.getPosition().getX());
				lMessage.setInteger("posY", lPingpongPlayer1.getPosition().getY());

				getServer().sendToken(lConnector, lMessage);
				getServer().sendToken(lConnector1, lMessage);

				lMessage.setString("player", "playRight");
				lMessage.setInteger("posX", lPingpongPlayer2.getPosition().getX());
				lMessage.setInteger("posY", lPingpongPlayer2.getPosition().getY());

				getServer().sendToken(lConnector, lMessage);
				getServer().sendToken(lConnector1, lMessage);
			} else if ("playRight".equals(aJoystick)) {
				lMessage.setString("player", "playRight");
				lMessage.setInteger("posX", lPingpongPlayer2.getPosition().getX());
				lMessage.setInteger("posY", lPingpongPlayer2.getPosition().getY());

				getServer().sendToken(lConnector, lMessage);
				getServer().sendToken(lConnector1, lMessage);
			} else {
				lMessage.setString("player", "playLeft");
				lMessage.setInteger("posX", lPingpongPlayer1.getPosition().getX());
				lMessage.setInteger("posY", lPingpongPlayer1.getPosition().getY());

				getServer().sendToken(lConnector, lMessage);
				getServer().sendToken(lConnector1, lMessage);

			}
		} catch (Exception ex) {
			if (mLog.isDebugEnabled()) {
				mLog.error("playerUpdate. " + ex.getMessage());
			}
		}
	}

	// Optengo un conector dado su user 
	private WebSocketConnector connectorUser(String aUserName) {
		Collection<WebSocketConnector> lCollection = getServer().
				getAllConnectors().values();

		for (WebSocketConnector c : lCollection) {
			if (c.getString("username") != null
					&& c.getString("username").equals(aUserName)) {
				return c;
			}
		}
		return null;
	}

	// Mando actualizar los puntos de una partida dado un user (score)
	public void scoreUpdate(PingpongMatch aPingpongMatch) {
		try {
			Token lMessage = TokenFactory.createToken(getNamespace(), "score");
			String lUserName1 = aPingpongMatch.getPingpongPlayer(0).getPlayerName();
			String lUserName2 = aPingpongMatch.getPingpongPlayer(1).getPlayerName();
			int lScore1, lScore2;
			lUserName1 = aPingpongMatch.getPingpongPlayer(0).getPlayerName();
			lUserName2 = aPingpongMatch.getPingpongPlayer(1).getPlayerName();
			lScore1 = aPingpongMatch.getPingpongPlayer(0).getPlayerScore();
			lScore2 = aPingpongMatch.getPingpongPlayer(1).getPlayerScore();

			lMessage.setString("username1", lUserName1);
			lMessage.setString("username2", lUserName2);
			lMessage.setInteger("score1", lScore1);
			lMessage.setInteger("score2", lScore2);

			getServer().sendToken(connectorUser(lUserName1), lMessage);
			getServer().sendToken(connectorUser(lUserName2), lMessage);
		} catch (Exception ex) {
			if (mLog.isDebugEnabled()) {
				mLog.error("scoreUpdate. " + ex.getMessage());
			}
		}
	}

	// mando eliminar los player , score  cuando se elimina un partido , Ball , game over 
	private void updatePlayerScore(String aUserName) {
		try {
			Token lMessage = TokenFactory.createToken(getNamespace(), "objarea");
			lMessage.setBoolean("objarea", false);
			getServer().sendToken(connectorUser(aUserName), lMessage);

		} catch (Exception ex) {
			if (mLog.isDebugEnabled()) {
				mLog.error("updatePlayerScore. " + ex.getMessage());
			}
		}
	}

	// actualizo mi pelota
	private void updateBall(PingpongMatch aPingpongMatch) {
		try {
			String lUserName1 = aPingpongMatch.getPingpongPlayer(0).getPlayerName();
			String lUserName2 = aPingpongMatch.getPingpongPlayer(1).getPlayerName();
			Token lMessage = TokenFactory.createToken(getNamespace(), "ball");

			lMessage.setInteger("width",
					(int) aPingpongMatch.getBall().getDimension().getWidth());
			lMessage.setInteger("height",
					(int) aPingpongMatch.getBall().getDimension().getHeight());

			getServer().sendToken(connectorUser(lUserName1), lMessage);
			getServer().sendToken(connectorUser(lUserName2), lMessage);
		} catch (Exception ex) {
			if (mLog.isDebugEnabled()) {
				mLog.error("updateBall. " + ex.getMessage());
			}
		}
	}

	//Aqui es donde le doy la posicion a mi pelota
	public void moveBall(PingpongMatch aPingpongMatch) {
		try {
			Token lMessage = TokenFactory.createToken(getNamespace(), "moveball");
			lMessage.setInteger("posX",
					(int) aPingpongMatch.getBall().getPosition().getX());
			lMessage.setInteger("posY",
					(int) aPingpongMatch.getBall().getPosition().getY());

			String lUserName1 = aPingpongMatch.getPingpongPlayer(0).getPlayerName();
			String lUserName2 = aPingpongMatch.getPingpongPlayer(1).getPlayerName();

			getServer().sendToken(connectorUser(lUserName1), lMessage);
			getServer().sendToken(connectorUser(lUserName2), lMessage);
		} catch (Exception ex) {
			if (mLog.isDebugEnabled()) {
				mLog.error("moveBall. " + ex.getMessage());
			}
		}
	}

	//Aqui activo y desctivo el Game Over
	public void gameOver(PingpongMatch aPingpongMatch, boolean aBool) {
		try {
			Token lMessage = TokenFactory.createToken(getNamespace(), "gameover");
			lMessage.setBoolean("gameover", aBool);
			for (PingpongPlayer lPlayer : aPingpongMatch.getPingpongPlayerList()) {

				if (lPlayer.getPlayerScore() == 10) {
					lMessage.setString("message", "Congrats");
					getServer().sendToken(connectorUser(lPlayer.getPlayerName()),
							lMessage);
				} else {

					lMessage.setString("message", "Game Over");
					getServer().sendToken(connectorUser(lPlayer.getPlayerName()),
							lMessage);
				}
			}

			if (aBool) {
				updateValueDB(aPingpongMatch);
			}
		} catch (Exception ex) {
			if (mLog.isDebugEnabled()) {
				mLog.error("gameOver. " + ex.getMessage());
			}
		}
	}

	// Muestro en el cliente el contador 3-2-1
	public void initCounter(PingpongMatch aPingpongMatch, int aCount) {
		try {
			Token lMessage = TokenFactory.createToken(getNamespace(), "counter");
			lMessage.setInteger("counter", aCount);

			getServer().sendToken(
					connectorUser(
					aPingpongMatch.getPingpongPlayer(0).getPlayerName()), lMessage);
			getServer().sendToken(
					connectorUser(
					aPingpongMatch.getPingpongPlayer(1).getPlayerName()), lMessage);

		} catch (Exception ex) {
			if (mLog.isDebugEnabled()) {
				mLog.error("initCounter. " + ex.getMessage());
			}
		}
	}

	//activar el sonido 
	public void sound(PingpongMatch aPingpongMatch, int aSound) {
		try {
			Token lMessage = TokenFactory.createToken(getNamespace(), "sound");
			lMessage.setInteger("sound", aSound);
			WebSocketConnector lconector1 = connectorUser(
					aPingpongMatch.getPingpongPlayer(0).getPlayerName());
			WebSocketConnector lconector2 = connectorUser(
					aPingpongMatch.getPingpongPlayer(1).getPlayerName());
			if (lconector1.getBoolean("sound")) {
				getServer().sendToken(lconector1, lMessage);
			}
			if (lconector2.getBoolean("sound")) {
				getServer().sendToken(lconector2, lMessage);
			}


		} catch (Exception ex) {
			if (mLog.isDebugEnabled()) {
				mLog.error("sound. " + ex.getMessage());
			}
		}
	}

	/*------------------------------------mongoDB-------------------------------*/
	public void updateValueDB(PingpongMatch aPingpongMatch) {
		try {
			for (PingpongPlayer lPlayer : aPingpongMatch.getPingpongPlayerList()) {
				if (lPlayer.getPlayerScore() == 10) {
					mUserServiceImpl.updateValue(lPlayer.getPlayerName(), 1, 0);
				} else {
					mUserServiceImpl.updateValue(lPlayer.getPlayerName(), 0, 1);
				}
			}

			ranking();
		} catch (Exception ex) {
			if (mLog.isDebugEnabled()) {
				mLog.error("updateValueDB. " + ex.getMessage());
			}
		}
	}

	// Mando la lista del ranking para cada conector
	private void ranking() {
		try {
			Collection<WebSocketConnector> lCollection = getServer().
					getAllConnectors().values();
			Token lMessage = orderRanking();

			for (WebSocketConnector c : lCollection) {
				getServer().sendToken(c, lMessage);
			}
		} catch (Exception ex) {
			if (mLog.isDebugEnabled()) {
				mLog.error("ranking. " + ex.getMessage());
			}
		}
	}

	private Token orderRanking() {
		DBCursor lDBCursor = mUserServiceImpl.getProfileList();
		List<User> lListUser = new ArrayList<User>();

		for (DBObject lDB : lDBCursor) {
			if (Integer.parseInt(lDB.get("wins").toString())
					+ Integer.parseInt(lDB.get("lost").toString()) > 0) {
				User IUser = new User(lDB.get("user").toString(), "",
						Integer.parseInt(lDB.get("wins").toString()),
						Integer.parseInt(lDB.get("lost").toString()));
				lListUser.add(IUser);
			}
		}

		for (int lIndex = 0; lIndex < lListUser.size(); lIndex++) {
			for (int lIndexa = lIndex + 1; lIndexa
					< lListUser.size(); lIndexa++) {
				if (lListUser.get(lIndex).getWins()
						- lListUser.get(lIndex).getLost()
						< lListUser.get(lIndexa).getWins()
						- lListUser.get(lIndexa).getLost()) {
					lListUser.add(lIndex, lListUser.get(lIndexa));
					lListUser.remove(lIndexa + 1);
				}
			}
		}

		Token lMessage = TokenFactory.createToken(getNamespace(), "ranking");
		List<String> lLU = new ArrayList<String>();
		List<Integer> lLW = new ArrayList<Integer>();
		List<Integer> lLL = new ArrayList<Integer>();

		for (User lUser : lListUser) {
			lLU.add(lUser.getUserName());
			lLW.add(lUser.getWins());
			lLL.add(lUser.getLost());
		}
		lMessage.setList("username", lLU);
		lMessage.setList("wins", lLW);
		lMessage.setList("lost", lLL);

		return lMessage;
	}
}
