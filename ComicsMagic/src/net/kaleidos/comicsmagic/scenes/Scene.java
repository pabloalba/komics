package net.kaleidos.comicsmagic.scenes;

public class Scene {
	float x;
	float y;
	float zoom;

	public Scene(float x, float y, float zoom) {
		super();
		this.x = x;
		this.y = y;
		this.zoom = zoom;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getZoom() {
		return zoom;
	}

	public void setZoom(float zoom) {
		this.zoom = zoom;
	}

}
