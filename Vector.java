public class Vector {
	float x, y, z;
	
	public Vector() {
		x = 0.0f;
		y = 0.0f;
		z = 0.0f;
	}
	
	public Vector(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector(Vector v) {
		x = v.x;
		y = v.y;
		z = v.z;
	}
	
	public void setTo(Vector v) {
		x = v.x;
		y = v.y;
		z = v.z;
	}
	
	public void setTo(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void add(Vector v) {
		x += v.x;
		y += v.y;
		z += v.z;
	}
	
	public Vector times(float k) {
		Vector v = new Vector();
		v.x = k*x;
		v.y = k*y;
		v.z = k*z;
		
		return v;
	}
	
	public void multiply(float k) {
		x *= k;
		y *= k;
		z *= k;
	}
	
	public float norm() {
		return (float) Math.sqrt(x*x + y*y + z*z);
	}
	
	public float dot(Vector v) {
		return x*v.x + y*v.y + z*v.z;
	}
	
	public float angle(Vector v) {
		float product = dot(v) / (norm()*v.norm()); // Normalized dot product of two vectors
		return (float)Math.acos(product);
	}
}
