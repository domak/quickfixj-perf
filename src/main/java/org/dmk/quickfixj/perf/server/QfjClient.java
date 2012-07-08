package org.dmk.quickfixj.perf.server;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.Application;
import quickfix.DefaultMessageFactory;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.FileLogFactory;
import quickfix.FileStoreFactory;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.LogFactory;
import quickfix.Message;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;
import quickfix.UnsupportedMessageType;
import quickfix.field.ClOrdID;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;
import quickfix.fix44.Logon;
import quickfix.fix44.NewOrderSingle;

public class QfjClient implements Application {

	private static final int ITER_MAX = 100000;

	/**
	 * @param args
	 */
	public static void main(String args[]) throws Exception {

		// FooApplication is your class that implements the Application interface
		Application application = new QfjClient();

		SessionSettings settings = new SessionSettings(Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("client.cfg"));
		MessageStoreFactory storeFactory = new FileStoreFactory(settings);
		LogFactory logFactory = new FileLogFactory(settings);

		// MessageStoreFactory storeFactory = new MemoryStoreFactory();
		// LogFactory logFactory = new SLF4JLogFactory(settings);

		MessageFactory messageFactory = new DefaultMessageFactory();
		SocketInitiator initiator = new SocketInitiator(application, storeFactory, settings, logFactory, messageFactory);
		initiator.start();
		while (true) {
			// void
		}
	}

	// -------------------------------------------------------------------------
	private static Logger logger = LoggerFactory.getLogger(QfjClient.class);
	private int counter;
	private long sendTime;
	private long cumTime;

	@Override
	public void onCreate(SessionID sessionId) {
		logger.debug("on create - session: {}", sessionId);
		try {
			Session.sendToTarget(new Logon(), sessionId);
		} catch (SessionNotFound e) {
			logger.error("cannot find session", e);
		}
	}

	@Override
	public void onLogon(SessionID sessionId) {
		logger.debug("on logon - session: {}", sessionId);
		sendNewOrder(sessionId);
	}

	@Override
	public void onLogout(SessionID sessionId) {
		logger.debug("on logout - session: {}", sessionId);

	}

	@Override
	public void toAdmin(Message message, SessionID sessionId) {
		logger.debug("to admin - message: {} - session: {}", message, sessionId);
	}

	@Override
	public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat,
			IncorrectTagValue, RejectLogon {
		logger.debug("from admin - message: {} - session: {}", message, sessionId);

	}

	@Override
	public void toApp(Message message, SessionID sessionId) throws DoNotSend {
		logger.debug("to app - message: {} - session: {}", message, sessionId);
	}

	@Override
	public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat,
			IncorrectTagValue, UnsupportedMessageType {
		cumTime += (System.nanoTime() - sendTime) / 1000;
		logger.debug("from app - message: {} - session: {}", message, sessionId);
		sendNewOrder(sessionId);
	}

	private void sendNewOrder(SessionID sessionId) {
		if (counter == ITER_MAX) {
			System.out.println(cumTime / counter + " us / order");
			System.exit(0);
		}
		NewOrderSingle message = new NewOrderSingle(new ClOrdID(Integer.toString(counter++)), new Side(Side.BUY),
				new TransactTime(new Date()), new OrdType(OrdType.MARKET));
		message.setString(Symbol.FIELD, "FP");
		message.setDouble(OrderQty.FIELD, 1);
		try {
			sendTime = System.nanoTime();
			Session.sendToTarget(message, sessionId);
		} catch (SessionNotFound e) {
			logger.error("cannot find session", e);
		}

	}
}
