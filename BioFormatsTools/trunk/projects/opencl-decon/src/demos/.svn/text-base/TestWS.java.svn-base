package demos;

import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.imagejdev.api.FHTEJBService;
import com.caucho.hessian.client.HessianProxyFactory;
import ij.IJ;
import ij.plugin.PlugIn;

public class TestWS implements PlugIn {
	public boolean DEBUG = false;


	public void run(String arg) {

		// set up the service

		try {
			String url = "http://ws.cbios.co:8080/EJBHessianFHT/FHTEJBService";

			// Class loader switch
			ClassLoader bundleClassLoader = this.getClass().getClassLoader();
			if (DEBUG)
				IJ.showMessage("BundleClassLoader: "
						+ bundleClassLoader.toString());
			ClassLoader originalClassLoader = Thread.currentThread()
					.getContextClassLoader();
			if (DEBUG)
				IJ.showMessage("OriginalClassLoader: "
						+ originalClassLoader.toString());

			// Set class loader
			Thread.currentThread().setContextClassLoader(bundleClassLoader);

			// invoke my code
			HessianProxyFactory factory = new HessianProxyFactory();
			FHTEJBService fhtejbservice = (FHTEJBService) factory.create(FHTEJBService.class,
					url);
			String results = fhtejbservice.getTestMessage();
			IJ.showMessage("Test WebServices returned greeting message "
					+ results);

			// switch classloader back
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		} catch (MalformedURLException ex) {
			Logger.getLogger(Iterative3DDeconWS.class.getName()).log(
					Level.SEVERE, null, ex);
		}

	}

	public static void main(String[] args) {
		// run the service
		new TestWS().run(null);
	}

}
