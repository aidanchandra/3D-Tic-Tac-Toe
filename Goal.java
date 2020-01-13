
public class Goal {

    public Coordinate[] points;

    public Goal() {
        this.points = new Coordinate[4];
    }

    public void set(int index, Coordinate point) {
        this.points[index] = point;
    }

    public boolean contains(Coordinate point) {
        // If this goal contains this point.
        return this.contains(point.x, point.y, point.z);
    }

    public boolean contains(int x, int y, int z) {
        for (Coordinate point : this.points) {
            if (point.x == x && point.y == y && point.z == z) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isMultiLevel() {
        return this.points[0].z != this.points[3].z; 
    }
    
    public boolean hasCorners() {
        for (Coordinate point : this.points) {
            if (point.isCorner()) {
                return true;
            }
        }
        return false;
    }

}
