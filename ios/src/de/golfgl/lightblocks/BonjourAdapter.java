package de.golfgl.lightblocks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;

import org.robovm.apple.foundation.NSData;
import org.robovm.apple.foundation.NSDictionary;
import org.robovm.apple.foundation.NSInputStream;
import org.robovm.apple.foundation.NSNetService;
import org.robovm.apple.foundation.NSNetServiceBrowser;
import org.robovm.apple.foundation.NSNetServiceBrowserDelegate;
import org.robovm.apple.foundation.NSNetServiceDelegate;
import org.robovm.apple.foundation.NSNetServiceErrorUserInfo;
import org.robovm.apple.foundation.NSNumber;
import org.robovm.apple.foundation.NSOutputStream;
import org.robovm.apple.foundation.NSString;
import org.robovm.apple.uikit.UIDevice;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.golfgl.lightblocks.multiplayer.INsdHelper;
import de.golfgl.lightblocks.multiplayer.IRoomLocation;
import de.golfgl.lightblocks.multiplayer.KryonetRoomLocation;


public class BonjourAdapter implements INsdHelper {

    public static final String HTTP_SERVICE = "_http._tcp";
    public static final String TAG = "NSD";
    private final NSNetServiceBrowser browser;
    private final ConcurrentHashMap<String, InetAddress> currentServices;
    private final MyNetServiceDelegate myNetServiceDelegate;
    private NSNetService service;

    public BonjourAdapter() {
        currentServices = new ConcurrentHashMap<String, InetAddress>();

        myNetServiceDelegate = new MyNetServiceDelegate();

        browser = new NSNetServiceBrowser();
        browser.setDelegate(new NSNetServiceBrowserDelegate() {
            @Override
            public void willSearch(NSNetServiceBrowser nsNetServiceBrowser) {

            }

            @Override
            public void didStopSearch(NSNetServiceBrowser nsNetServiceBrowser) {

            }

            @Override
            public void didNotSearch(NSNetServiceBrowser nsNetServiceBrowser, NSDictionary<NSString, NSNumber> nsDictionary) {

            }

            @Override
            public void didFindDomain(NSNetServiceBrowser nsNetServiceBrowser, String s, boolean b) {

            }

            @Override
            public void didFindService(NSNetServiceBrowser nsNetServiceBrowser, NSNetService nsNetService, boolean b) {
                if (nsNetService.getName().startsWith(SERVICE_NAME)) {
                    Gdx.app.debug(TAG, "Service found: " + nsNetService.getName());
                    nsNetServiceBrowser.addStrongRef(nsNetService);
                    nsNetService.setDelegate(myNetServiceDelegate);
                    nsNetService.resolve(.3);
                }
            }

            @Override
            public void didRemoveDomain(NSNetServiceBrowser nsNetServiceBrowser, String s, boolean b) {

            }

            @Override
            public void didRemoveService(NSNetServiceBrowser nsNetServiceBrowser, NSNetService nsNetService, boolean b) {
                if (nsNetService.getName().startsWith(SERVICE_NAME))
                    synchronized (currentServices) {
                        Gdx.app.debug(TAG, "Service lost: " + nsNetService.getName());
                        currentServices.remove(nsNetService.getName().substring(SERVICE_NAME.length() + 1));
                    }
            }
        });
    }

    @Override
    public void registerService() {
        if (service == null) {
            service = new NSNetService("", HTTP_SERVICE, SERVICE_NAME + "-" + UIDevice.getCurrentDevice().getLocalizedModel() + "-" + MathUtils.random(100, 999));
            service.setDelegate(myNetServiceDelegate);
            service.publish();
        }
    }

    @Override
    public void unregisterService() {
        if (service != null) {
            service.stop();
            service = null;
        }
    }

    @Override
    public void startDiscovery() {
        browser.searchForServices(HTTP_SERVICE, "");
    }

    @Override
    public void stopDiscovery() {
        browser.stop();
    }

    @Override
    public List<IRoomLocation> getDiscoveredServices() {
        List<IRoomLocation> retVal = new LinkedList<>();

        synchronized (currentServices) {
            for (Map.Entry<String, InetAddress> entry : currentServices.entrySet()) {
                retVal.add(new KryonetRoomLocation(entry.getKey(), entry.getValue()));
            }
        }

        return retVal;
    }

    private class MyNetServiceDelegate implements NSNetServiceDelegate {
        @Override
        public void willPublish(NSNetService nsNetService) {

        }

        @Override
        public void didPublish(NSNetService nsNetService) {
            Gdx.app.debug(TAG, "Published service.");
        }

        @Override
        public void didNotPublish(NSNetService nsNetService, NSNetServiceErrorUserInfo nsNetServiceErrorUserInfo) {
            Gdx.app.debug(TAG, "Service not published.");
        }

        @Override
        public void willResolve(NSNetService nsNetService) {

        }

        @Override
        public void didResolve(NSNetService nsNetService) {
            if (nsNetService.getName().startsWith(SERVICE_NAME)) {
                try {
                    synchronized (currentServices) {
                        Gdx.app.debug(TAG, "Service resolved: " + nsNetService.getHostName());
                        currentServices.put(nsNetService.getName().substring(SERVICE_NAME.length() + 1), InetAddress.getByName
                                (nsNetService.getHostName()));
                    }
                    browser.removeStrongRef(nsNetService);
                } catch (UnknownHostException e) {
                    // eat
                }
            }
        }

        @Override
        public void didNotResolve(NSNetService nsNetService, NSNetServiceErrorUserInfo nsNetServiceErrorUserInfo) {

        }

        @Override
        public void didStop(NSNetService nsNetService) {
            Gdx.app.debug(TAG, "Published service stopped.");
        }

        @Override
        public void didUpdateTXTRecordData(NSNetService nsNetService, NSData nsData) {

        }

        @Override
        public void didAcceptConnection(NSNetService nsNetService, NSInputStream nsInputStream, NSOutputStream nsOutputStream) {

        }
    }
}
