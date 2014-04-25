import java.net.URL;


public final class ResourceLoader {
	
	public static URL getURL(String path) {
		URL url = ResourceLoader.class.getClassLoader().getResource(path);
		return url;
	}
	
}
