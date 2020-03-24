package com.mrp_v2.biomeborderviewer.util;

public class WallData {
	public Vector2Float a, b;
	public Color color;
	
	public WallData(Vector2Float a, Vector2Float b) {
		this.a = a;
		this.b = b;
	}
	
	public WallData(Vector2Float a, Vector2Float b, Color color) {
		this(a, b);
		this.color = color;
	}
	
	public int hashCode() {
		return a.hashCode() * b.hashCode();
	}
	
	public boolean equals(WallData other) {
		return a.equals(other.a) && b.equals(other.b) && color.equals(other.color);
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof WallData) {
			return equals((WallData)obj);
		}
		return false;
	}
}
