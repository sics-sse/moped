package fresta.pirte;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import fresta.link.Linker;
import fresta.port.instances.VirtualFrontWheelPPort;
import fresta.port.instances.VirtualRearWheelPPort;
import fresta.port.instances.VirtualPublishRPort;
import fresta.port.instances.VirtualLEDRPort;
import fresta.port.instances.VirtualVoltagePPort;
import fresta.port.instances.VirtualPositionPPort;
import fresta.port.instances.VirtualAcceleratorRPort;
import fresta.port.instances.VirtualSteeringRPort;
import autosar.RTE;
import sics.plugin.PlugInComponent;
import sics.port.EcuVirtualPPort;
import sics.port.EcuVirtualRPort;
import sics.port.Port;
import messages.InstallAckMessage;
import messages.InstallMessage;
import messages.LinkContextEntry;
import messages.LoadMessage;
import messages.Message;
import messages.MessageType;
import messages.PWMMessage;
import messages.PluginMessage;
import messages.UninstallAckMessage;
import messages.UninstallMessage;

public class PIRTE implements Runnable {
	private int id;
	private final Object lock = new Object();
	private List<Message> manageMsgs = new ArrayList<Message>();
	private List<Message> msgBuffer = new ArrayList<Message>(); //TODO: Is there a better way to avoid java.util.ConcurrentModificationException???
	private HashMap<Integer, Message> pluginMessages = new HashMap<Integer, Message>();
	
	private HashMap<String, Thread> runnablePlugins = new HashMap<String, Thread>();
	public static Hashtable<Integer, Port> vpports = new Hashtable<Integer, Port>();
	public static Hashtable<Integer, Port> vrports = new Hashtable<Integer, Port>();
	
	private Linker linker;
	
	public PIRTE(int id) {
		linker = new Linker(this);
		this.id = id;
		
		vrports.put(0, new VirtualPublishRPort(0));
		vrports.put(new Integer(3), new VirtualAcceleratorRPort(3));
		vrports.put(new Integer(4), new VirtualSteeringRPort(4));
		vpports.put(new Integer(5), new VirtualFrontWheelPPort(5));
		vpports.put(new Integer(6), new VirtualRearWheelPPort(6));
		vpports.put(new Integer(7), new VirtualVoltagePPort(7));
		vpports.put(new Integer(8), new VirtualPositionPPort(8));
		vrports.put(new Integer(9), new VirtualLEDRPort(9));
	}
	
	/**
	 * The Class PlugInLoader reads classes from a jar or zip file, and creates
	 * the corresponding classes. This includes inner classes of sub components
	 * that must also be included in the file. It is assumed that the main class
	 * is always the first entry in the file.
	 */
	class PlugInLoader extends ClassLoader {

		/**
		 * Install plug in.
		 * 
		 * @param jarFile
		 *            the jar file
		 * @return the plug in component
		 */
	    public PlugInComponent loadPlugIn(JarFile jarFile, String appname) {
		appname = appname.substring(0, appname.lastIndexOf("."));
		System.out.println("appname " + appname);
			Class<?> mainClass = null;
			try {
				for (Enumeration<JarEntry> e = jarFile.entries(); e
						.hasMoreElements();) {
					JarEntry entry = e.nextElement();

					// Skip directories
					if(entry.isDirectory()) {
						continue;		
					}
					
					// Skip non-class files
					String fileName = entry.getName();
					if (!fileName.endsWith(".class")) {
						continue;
					}
					
					// Extract full class name 
					String className = fileName.substring(0, fileName.length() - 6); 
					className = className.replace('/', '.');
					
					// Check if this class has already been loaded. 
					// If not, define and load
					Class<?> newClass;
					if (findLoadedClass(className) != null) {
					    System.out.println("already loaded " + className);
					    //newClass = this.loadClass(className);
					}
{
						InputStream is = jarFile.getInputStream(entry);
						ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
						int nextValue = is.read();
						while (-1 != nextValue) {
							byteStream.write(nextValue);
							nextValue = is.read();
						}
						byte classByte[] = byteStream.toByteArray();
						
						newClass = defineClass(className, classByte, 0,
								classByte.length);
					}

					// TODO: UGLY HACK (Should be replaced by scanning the Manifest file for main-entry
					System.out.println("class " + className);
					if (className.endsWith(appname))
					    mainClass = newClass;
				}
					
				jarFile.close();
				System.out.println("main Class" + mainClass);
//				Constructor con=mainClass.getDeclaredConstructor(new Class[]{String[].class});
				return ((PlugInComponent) mainClass.newInstance());
			} catch (Exception e) {
				System.out.println("Exception triggered");
				e.printStackTrace();
				return null;
			}
		}
	}
	
	protected PlugInLoader loader = new PlugInLoader();

    public PlugInComponent loadPlugIn(String location, String appname) {
		PlugInComponent plugin = null;
		try {
			JarFile jarFile = new JarFile(location);
			plugin = loader.loadPlugIn(jarFile, appname);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return plugin;
	}
	
	public void run() {
	    while(true) {
		try {
		    await();
		} catch (InterruptedException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
			
		// forward message
		//			ArrayList<Message> ackMessages = new ArrayList<Message>();
		//TODO: Is there a better way to avoid java.util.ConcurrentModificationException???
		synchronized(this) {
		    manageMsgs = new ArrayList<Message>();
		    for (Message msg : msgBuffer) {
			manageMsgs.add(msg);
		    }
		    msgBuffer.clear();
		}
			
		for(Message message : manageMsgs) {
		    int messageType = message.getMessageType();
		    System.out.println(">>> simulator/PIRTE 1 " + messageType);
		    switch(messageType) {
		    case MessageType.INSTALL:
			InstallMessage installMessage = (InstallMessage) message;
			byte pluginId = installMessage.getPluginId();
			HashMap<String, Integer> portInitialContext = installMessage.getPortInitialContext();
			String[] portInitialContextArray = convertMap2Array(portInitialContext);
			ArrayList<LinkContextEntry> linkContext = installMessage.getLinkContext();
			byte[] binaryFile = installMessage.getBinaryFile();
			// Save the binary to file
			String executablePluginName = installMessage.getExecutablePluginName();
			System.out.println("PIRTE received exe " + executablePluginName);
			System.out.println("PIRTE received local appId " + pluginId);

			int lastIndexOf = executablePluginName.lastIndexOf("/");
			String pluginName = executablePluginName.substring(lastIndexOf+1);
			String fileLocation = "ecus/ecu"+id+"/"+pluginName;
			File file = new File(fileLocation);
			FileOutputStream fos;
			try {
			    fos = new FileOutputStream(file);
			    fos.write(binaryFile);
			    fos.close();
			    // Initiate PlugInComponent
			    System.out.println("Will try to load jar file at " + fileLocation);
			    JarFile jarFile = new JarFile(fileLocation);
			    System.out.println("jarFile created");

			    Thread runnablePlugin = runnablePlugins.get(pluginName);
			    if (runnablePlugin != null) {
				System.out.println("stopping earlier thread");
				runnablePlugin.interrupt();
			    }

			    loader = new PlugInLoader();
			    PlugInComponent loadPlugIn = loader.loadPlugIn(jarFile, pluginName);
			    if(loadPlugIn == null) {
				System.out.println("loadedPlugin is null");
				System.exit(-1);
			    }
			    loadPlugIn.setPirte(this);
			    loadPlugIn.initPortInitContext(portInitialContextArray);
			    // Register Link Context
			    if(linkContext != null) {
				for(LinkContextEntry entry:linkContext) {
				    linker.link(entry);
				}
			    }
						
			    runnablePlugin = new Thread(loadPlugIn);
			    runnablePlugin.start();
		    System.out.println("<<< simulator/PIRTE 1 " + messageType);
			    RTE.getInstance().addRteMessage(
							    new InstallAckMessage(pluginId, pluginName));
						
			    // Register PlugIn in pirte
			    runnablePlugins.put(pluginName, runnablePlugin);
			} catch (FileNotFoundException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			} catch (IOException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}
			break;
		    case MessageType.UNINSTALL:
			UninstallMessage uninstallMessage = (UninstallMessage) message;
			String pluginName4Uninstall = uninstallMessage.getPluginName();
			if(pluginName4Uninstall.contains(".suite")) {
			    pluginName4Uninstall = pluginName4Uninstall.replace(".suite", ".zip");
			}
			System.out.println("uninstall: pluginname:"+pluginName4Uninstall);
			Thread runnablePlugin = runnablePlugins.get(pluginName4Uninstall);
			System.out.println("UNINSTALL thread " + runnablePlugin);
			//runnablePlugins.remove(runnablePlugin);
			runnablePlugin.interrupt();
			System.out.println("<<< simulator/PIRTE 1 " + messageType);
			RTE.getInstance().addRteMessage
			    (new UninstallAckMessage(pluginName4Uninstall));
			break;
		    case MessageType.LOAD:
			LoadMessage loadMessage = (LoadMessage) message;
			HashMap<String, Integer> portInitialContext4Load = loadMessage.getPortInitialContext();
			String[] portInitialContextArray4Load = convertMap2Array(portInitialContext4Load);
			ArrayList<LinkContextEntry> linkContext4Load = loadMessage.getLinkContext();
			byte[] binaryFile4Load = loadMessage.getBinaryFile();
			// Save the binary to file
			String executablePluginName4Load = loadMessage.getExecutablePluginName();
			int lastIndexOf4Load = executablePluginName4Load.lastIndexOf("/");
			String pluginName4Load = executablePluginName4Load.substring(lastIndexOf4Load+1);
			String fileLocation4Load = "ecus/ecu"+id+"/"+pluginName4Load;
			File file4Load = new File(fileLocation4Load);
			FileOutputStream fos4Uninstall;
			try {
			    fos4Uninstall = new FileOutputStream(file4Load);
			    fos4Uninstall.write(binaryFile4Load);
			    fos4Uninstall.close();
			    // Initiate PlugInComponent
			    PlugInComponent loadPlugIn = loader.loadPlugIn(new JarFile(fileLocation4Load), pluginName4Load);
			    if(loadPlugIn == null) {
				System.out.println("loadedPlugin is null");
				System.exit(-1);
			    }
			    loadPlugIn.setPirte(this);
			    loadPlugIn.initPortInitContext(portInitialContextArray4Load);
			    // Register Link Context
			    if(linkContext4Load != null) {
				for(LinkContextEntry entry:linkContext4Load) {
				    linker.link(entry);
				}
			    }
						
						
			    Thread runnablePlugin4Load = new Thread(loadPlugIn);
			    runnablePlugin4Load.start();
						
			    // Register PlugIn in pirte
			    runnablePlugins.put(pluginName4Load, runnablePlugin4Load);
			} catch (FileNotFoundException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			} catch (IOException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}
			break;
		    default:
			System.out.println("Error: wrong message type when PIRTE handles management messages");
		    }
		}
		//			manageMsgs.clear();
	    }
	}

	public void addMessage(Message message) {
	    //	    Thread.dumpStack();
	    System.out.println(">>> simulator/PIRTE 2 " + message.getMessageType());
		int messageType = message.getMessageType();
		switch(messageType) {
		case MessageType.INSTALL:
		case MessageType.UNINSTALL:
		case MessageType.LOAD:
//			System.out.println("package arrives in PIRTE");
//			manageMsgs.add(message);
			msgBuffer.add(message);
			wakeup();
			break;
		case MessageType.PLUGIN_MESSAGE:
			PluginMessage pluginMessage = (PluginMessage) message;
			int remotePortId = pluginMessage.getRemotePortId();
			pluginMessages.put(remotePortId, message);
			break;
		default:
			System.out.println("Error: wrong message type " + messageType);
		}
	}
	
	private void wakeup() {
		synchronized (lock) {
            lock.notify();
        }
	}
	
	private void await() throws InterruptedException {
		synchronized(lock) {
			lock.wait();
		}
	}
	
	private String[] convertMap2Array(HashMap<String, Integer> portInitialContext) {
		String[] res = new String[portInitialContext.size()*2];
		int index = 0;
		Iterator<Entry<String, Integer>> iterator = portInitialContext.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String, Integer> entry = iterator.next();
			String key = entry.getKey();
			res[index++] = key;
			String value = ""+entry.getValue();
			res[index++] = value;
		}
		
		return res;
	}
	
	public void deliverValue(int pportId, int value) {
		int vrportId = linker.getVirtualRPortId(pportId);
		EcuVirtualRPort vrport = (EcuVirtualRPort) vrports.get(vrportId);
		if (vrport == null) {
			System.out.println("Error: Fail to get Virtual Port "+vrportId);
			return;
		}
		vrport.deliver(new Integer(value));
	}
	
	public void deliverValue(int pportId, String value) {
		int vrportId = linker.getVirtualRPortId(pportId);
		EcuVirtualRPort vrport = (EcuVirtualRPort) vrports.get(vrportId);
		vrport.deliver(value);
	}
	
	public int fetchIntVal(int rportId) {
		int vpportId = linker.getVirtualPPortId(rportId);
		EcuVirtualPPort vpport = (EcuVirtualPPort) vpports.get(vpportId);
		Integer intVal = (Integer) vpport.deliver();
		return intVal.intValue();
	}
	
	public long fetchLongVal(int rportId) {
		int vpportId = linker.getVirtualPPortId(rportId);
		EcuVirtualPPort vpport = (EcuVirtualPPort) vpports.get(vpportId);
		Long longVal = (Long) vpport.deliver();
		return longVal.longValue();
	}
	
	public String fetchStringVal(int rportId) {
		int vpportId = linker.getVirtualPPortId(rportId);
		EcuVirtualPPort vpport = (EcuVirtualPPort) vpports.get(vpportId);
		String val = (String) vpport.deliver();
		return val;
	}
	
	public Object fetchVal(int rportId) {
		int vpportId = linker.getVirtualPPortId(rportId);
		EcuVirtualPPort vpport = (EcuVirtualPPort) vpports.get(vpportId);
		Object res = vpport.deliver(rportId);
		return res;
	}

}
