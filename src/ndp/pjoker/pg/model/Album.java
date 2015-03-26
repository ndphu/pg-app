package ndp.pjoker.pg.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Album {
	private long id;
	private String name;
	private String cover;

	public Album(JSONObject obj) throws JSONException {
		this.id = obj.getLong("id");
		this.name = obj.getString("name");
		this.cover = obj.getString("cover");
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCover() {
		return cover;
	}

	public void setCover(String coverId) {
		this.cover = coverId;
	}

}
