import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Queue;

/**
 * Node represents an intersection in the road graph. It stores its ID and its
 * location, as well as all the segments that it connects to. It knows how to
 * draw itself, and has an informative toString method.
 * 
 * @author tony
 */
public class Node implements  Comparable<Node>{

	public final int nodeID;
	public final Location location;
	public final Collection<Segment> segments;
	
	//feilds for Astar
	
	private boolean nodeVisited = false;
	private Node perentNode;
	private double totalCost; 
	private double estimateDist;
	
	
	//stuff for articulation points 
	
	private boolean apIsVisited = false;
	private Node apFromNode;
	private int depth = Integer.MAX_VALUE;
	private int reachBack;
	private Queue<Node> children;
	
	public HashSet<Integer> nextToNodeIDs = new HashSet<Integer>();
	

	public Node(int nodeID, double lat, double lon) {
		this.nodeID = nodeID;
		this.location = Location.newFromLatLon(lat, lon);
		this.segments = new HashSet<Segment>();
	}
	
	//general node methods
	public Location getLocation() {
		return this.location;
	    }
	
	
	//node compare method
	  public int compareTo(Node nodeTwo) {
			if (this.estimateDist < nodeTwo.goalEstimate()) {
			    return -1;
			}else if (this.estimateDist == nodeTwo.goalEstimate()) {
			    return 0;
			} else {
			    return 1;
			}
		    }
	//Astar methods
	
	public double getTotalCostToNode() {
		return this.totalCost;
	    }
	
	public int getNodeID() {
		
		return this.nodeID;
	}
	
	
	public void setTotalCostToNode(double tCost) {
		this.totalCost = tCost;
		
	}
	
	  public void setEstimateToNode (double estimatedDist) {
			this.estimateDist = estimatedDist;
		    }
	
	public void setPerent(Node fromNode){
		this.perentNode = fromNode;
	    }
	
	public boolean isVisited() {
		return nodeVisited;
	    }
	
	public Node getPerentNode() {
		return this.perentNode;
		
	}
	
	public void makeVisited(boolean visit) {
		this.nodeVisited = visit;
	    }
	
	
	
	
	 public double goalEstimate(){
			return this.estimateDist;
		    }
	
	 public void addSegment(Segment seg) {
			segments.add(seg);
		}
	 
	 
	 
	 //articulation point methods ***************************
	 
	 public void resetAP(){
		 
		 this.depth = Integer.MAX_VALUE;
		 this.apIsVisited = false;
		 this.apFromNode = null;
		 this.reachBack = Integer.MAX_VALUE;
		 this.children = null;
		 
	 }
	 
	 public HashSet<Integer> getNextToNodeIDs(){
		 
		return this.nextToNodeIDs;
	 }
	 
	 public void addNextToNodesID(int id) {
		 
		 this.nextToNodeIDs.add(id);
		 
	 }
	 
	 public boolean apGetVisited() {
		 
		 
		 return this.apIsVisited;
	 }
	 
	 public Node getFromApNode(){
		 
		 return this.apFromNode;
	 }
	 
	 
	 public void setApVisited(boolean B) {
		 
		 this.apIsVisited = B;
	 }
	 
	 
	 public void setFromApNode(Node f) {
		 
		 
		 this.apFromNode = f;
	 }
	 
	 public void setDepth(int newDepth) {
		 
		 this.depth = newDepth;
		 
	 }
	 
	 public int getDepth() {
		 
		 return this.depth;	 
		
	 }
	 
	 public int getReachBack() {
		 
		 return this.reachBack;
	 }
	 
	 public void setReachBack(int r) {
		 
		 this.reachBack = r;
	 }
	 
	 public Queue<Node> getChildren(){
		 
		 return this.children;
	 }
	 
	 public void setChildren(Queue<Node> qu) {
		 
		 this.children = qu;
	 }
	 
	 
	 //*******************************************************

	

	public void draw(Graphics g, Dimension area, Location origin, double scale) {
		Point p = location.asPoint(origin, scale);

		// for efficiency, don't render nodes that are off-screen.
		if (p.x < 0 || p.x > area.width || p.y < 0 || p.y > area.height)
			return;

		int size = (int) (Mapper.NODE_GRADIENT * Math.log(scale) + Mapper.NODE_INTERCEPT);
		g.fillRect(p.x - size / 2, p.y - size / 2, size, size);
	}

	public String toString() {
		Set<String> edges = new HashSet<String>();
		for (Segment s : segments) {
			if (!edges.contains(s.road.name))
				edges.add(s.road.name);
		}

		String str = "ID: " + nodeID + "  loc: " + location + "\nroads: ";
		for (String e : edges) {
			str += e + ", ";
		}
		return str.substring(0, str.length() - 2);
	}
}

// code for COMP261 assignments