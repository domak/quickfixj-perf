package org.dmk.quickfixj.perf.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.Acceptor;
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
import quickfix.SocketAcceptor;
import quickfix.UnsupportedMessageType;
import quickfix.field.AvgPx;
import quickfix.field.CumQty;
import quickfix.field.ExecID;
import quickfix.field.ExecType;
import quickfix.field.LeavesQty;
import quickfix.field.OrdStatus;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.fix50.ExecutionReport;

public class QfjServer implements Application {

	public static void main(String args[]) throws Exception {

		// FooApplication is your class that implements the Application interface
		Application application = new QfjServer();

		SessionSettings settings = new SessionSettings(Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("server.cfg"));
		// MessageStoreFactory storeFactory = new MemoryStoreFactory();
		// LogFactory logFactory = new SLF4JLogFactory(settings);
		MessageStoreFactory storeFactory = new FileStoreFactory(settings);
		LogFactory logFactory = new FileLogFactory(settings);

		MessageFactory messageFactory = new DefaultMessageFactory();
		Acceptor acceptor = new SocketAcceptor(application, storeFactory, settings, logFactory, messageFactory);
		acceptor.start();
	}

	// -------------------------------------------------------------------------
	private static Logger logger = LoggerFactory.getLogger(QfjServer.class);
	private int counter;

	@Override
	public void onCreate(SessionID sessionId) {
		logger.debug("on create - session: {}", sessionId);
	}

	@Override
	public void onLogon(SessionID sessionId) {
		logger.debug("on logon - session: {}", sessionId);

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
		logger.debug("from app - message: {} - session: {}", message, sessionId);
		Message ack = new ExecutionReport(new OrderID("order-" + counter++), new ExecID(Integer.toString(counter++)),
				new ExecType(ExecType.NEW), new OrdStatus(OrdStatus.NEW), new Side(message.getChar(Side.FIELD)),
				new LeavesQty(message.getDouble(OrderQty.FIELD)), new CumQty(message.getDouble(OrderQty.FIELD)));
		ack.setString(Symbol.FIELD, message.getString(Symbol.FIELD));
		ack.setDouble(AvgPx.FIELD, 0);

		try {
			Session.sendToTarget(ack, sessionId);
		} catch (SessionNotFound e) {
			logger.error("session not found - id: {}", sessionId);
		}
	}
}
