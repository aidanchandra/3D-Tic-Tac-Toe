
public class Coordinate {

    public int x;
    public int y;
    public int z;

    public Coordinate(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString() {
        return "(" + this.x + " " + this.y + " " + this.z + ")";
    }

    public boolean isCorner() {
        return (this.x == 3 || this.x == 0) && (this.y == 3 || this.y == 0) && (this.z == 3 || this.z == 0);
    }

    public boolean isMiddle() {
        return (this.x == 1 || this.x == 2) && (this.y == 1 || this.y == 2) && (this.z == 1 || this.z == 2);
    }

}
