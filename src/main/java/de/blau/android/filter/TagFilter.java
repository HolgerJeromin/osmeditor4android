package de.blau.android.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import de.blau.android.R;
import de.blau.android.osm.Node;
import de.blau.android.osm.OsmElement;
import de.blau.android.osm.Relation;
import de.blau.android.osm.RelationMember;
import de.blau.android.osm.Way;
import de.blau.android.prefs.Preferences;

/**
 * Filter plus UI for filtering on tags
 * NOTE: the relevant ways should be processed before nodes 
 * @author simon
 *
 */
public class TagFilter extends Filter {
	private static final String DEFAULT_FILTER = "Default";

	private class FilterEntry implements Serializable {
		private static final long serialVersionUID = 2L;
		
		boolean active = false;
		boolean include = false;
		boolean allElements = false;
		boolean withMembers = false;
		String type;
		Pattern key;
		Pattern value;
		
		FilterEntry(boolean include, String type, String key, String value, boolean active) {
			this.include = include;
			allElements = "*".equals(type); // just check this once
			withMembers = type.endsWith("+");
			this.type = withMembers ? type.substring(0, type.length()-1) : type;
			this.key = key != null && !"".equals(key) ? Pattern.compile(key) : null;
			this.value = value != null && !"".equals(value) ? Pattern.compile(value) : null;
			this.active = active;
		}
		
		boolean match(String type, String key, String value) {
			if (allElements || this.type.equals(type)) {
				Matcher keyMatcher = null;
				if (this.key != null) {
					if (key == null) {
						return false;
					}
					keyMatcher = this.key.matcher(key);
				}
				Matcher valueMatcher = null;
				if (this.value != null) {
					if (value == null) {
						return false;
					}
					valueMatcher = this.value.matcher(value);
				}
				return (keyMatcher == null || keyMatcher.matches()) && (valueMatcher == null || valueMatcher.matches());
			}
			return false;
		}
		
		@Override
		public String toString() {
			return "Active " + active + " include " + include + " type " + type + " key " + key + " value " + value;
		}
	}
	
	ArrayList<FilterEntry> filter = new ArrayList<FilterEntry>();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final static String DEBUG_TAG = "TagFilter";
	
	private boolean enabled = true;
	transient private SQLiteDatabase mDatabase;

	public TagFilter(Context context) {
		super();
		init(context);
		//
		filter.clear();
		// filter, include INTEGER DEFAULT 0, type TEXT DEFAULT '*', key TEXT DEFAULT '*', value DEFAULT '*', active INTEGER ;
		
		Cursor dbresult = mDatabase.query(
				"filterentries",
				new String[] {"include", "type", "key", "value", "active"},
				"filter = ?", new String[] {DEFAULT_FILTER}, null, null, null);
		dbresult.moveToFirst();
		for (int i = 0; i < dbresult.getCount(); i++) {
			filter.add(new FilterEntry(
					dbresult.getInt(0) == 1,
					dbresult.getString(1),
					dbresult.getString(2),
					dbresult.getString(3),
					dbresult.getInt(4) == 1));
			dbresult.moveToNext();
		}
		dbresult.close();
	}
	
	@Override
	public void init(Context context) {
		mDatabase = new TagFilterDatabaseHelper(context).getReadableDatabase();
	}
	
	private boolean filter(OsmElement e) {
		boolean include = false;
		String type = e.getName();
		for (FilterEntry f:filter) {
			if (f.active) {
				boolean match = false;
				SortedMap<String,String>tags = e.getTags();
				if (tags != null && tags.size() > 0) {
					for (Entry<String,String>t:tags.entrySet()) {
						if (f.match(type,t.getKey(),t.getValue())) {
							match = true;
							break;
						}
					}
				} else {
					match = f.match(type,null,null);
				}
				if (match) {
					// Log.d(DEBUG_TAG,e.getDescription(true) + " matched " + f.toString());
					include = f.include; //FIXME should relation membership be able to override this?
					break;
				}
			}
		}
		
		if (!include) {
			// check if it is a relation member 
			List<Relation> parents = e.getParentRelations();
			if (parents != null) {
				for (Relation r:parents) {
					include = include || include(r, false);
				}
			}
		}
		// Log.d(DEBUG_TAG,e.getDescription() + " include: " + include);
		return include;
	}
	
	@Override
	public boolean include(Node node, boolean selected) {
		if (!enabled || selected) {
			return true;
		}
		Boolean include = cachedNodes.get(node);
		if (include != null) {
			return include;
		}

		include = filter(node);
		
		cachedNodes.put(node,include);
		return include;
	}

	@Override
	public boolean include(Way way, boolean selected) {
		if (!enabled || selected) {
			return true;
		}
		Boolean include = cachedWays.get(way);
		if (include != null) {
			return include;
		}
		
		include = filter(way);
		
		for (Node n:way.getNodes()) {
			Boolean includeNode = cachedNodes.get(n);
			if (includeNode == null || (include && !includeNode)) { 
				// if not originally included overwrite now
				if (!include && (n.hasTags() || n.hasParentRelations())) { // no entry yet so we have to check tags and relations
					include(n,false);
					continue;
				}
				cachedNodes.put(n,include);
			} 
		}
		cachedWays.put(way,include);
		
		return include;
	}

	@Override
	public boolean include(Relation relation, boolean selected) {
		if (!enabled || selected) {
			return true;
		}
		Boolean include = cachedRelations.get(relation);
		if (include != null) {
			return include;
		}
		
		include = filter(relation);
		
		cachedRelations.put(relation, include);
		List<RelationMember> members = relation.getMembers();
		if (members != null) {
			for (RelationMember rm:members) {
				OsmElement element = rm.getElement();
				if (element != null) {
					if (element instanceof Way) {
						Way w = (Way)element;
						Boolean includeWay = cachedWays.get(w);
						if (includeWay == null || (include && !includeWay)) { 
							// if not originally included overwrite now
							for (Node n:w.getNodes()) {
								cachedNodes.put(n,include);
							}
							cachedWays.put(w,include);
						} 
					} else if (element instanceof Node) { 
						Node n = (Node)element;
						Boolean includeNode = cachedNodes.get(n);
						if (includeNode == null || (include && !includeNode)) { 
							// if not originally included overwrite now
							cachedNodes.put(n,include);
						} 
					} else if (element instanceof Relation) {
						// FIXME
					}
				}
			}
		}
		
		return include;
	}
	

	/**
     * Tag filter controls
     */
    transient private FloatingActionButton tagFilterButton;
    transient ViewGroup parent;
    transient RelativeLayout controls;
    transient Update update;
	
	
    @Override
	public void addControls(ViewGroup layout, final Update update) {
    	Log.d(DEBUG_TAG, "adding filter controls");
    	this.parent = layout;
    	this.update = update;
		tagFilterButton = (FloatingActionButton)parent.findViewById(R.id.tagFilterButton);
		final Context context = layout.getContext();
    	// we weren't already added ...
		if (tagFilterButton == null) {
			Preferences prefs = new Preferences(context);
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			controls = (RelativeLayout)inflater.inflate(prefs.followGPSbuttonPosition().equals("LEFT")?R.layout.tagfilter_controls_right:R.layout.tagfilter_controls_left, layout);
			tagFilterButton = (FloatingActionButton)controls.findViewById(R.id.tagFilterButton);
		}
		
		tagFilterButton.setClickable(true);
		tagFilterButton.setOnClickListener(new OnClickListener() {
		    @Override
			public void onClick(View b) {
		    	Log.d(DEBUG_TAG,"Button clicked");
		    	// setupControls(true);
		    	TagFilterActivity.start(context, DEFAULT_FILTER);
		    }
		});
		setupControls(false);
	}
    
    private void setupControls(boolean toggle) {
    	enabled = toggle ? !enabled : enabled;
    	update.execute();
    }
	
    @Override
    public void removeControls() {
    	if (parent != null && controls != null) {
    		parent.removeView(controls);
    	}
    }
    
	@Override
	public void hideControls() {
		if(tagFilterButton != null) {
			tagFilterButton.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void showControls() {
		if(tagFilterButton != null) {
			tagFilterButton.setVisibility(View.VISIBLE);
		}
	}	
}
