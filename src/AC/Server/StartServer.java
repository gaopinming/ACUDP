package AC.Server;

public class StartServer {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		new Thread() {

			public void run() {

				try {
					ACServer.openServer();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};

		}.start();
	}

}
