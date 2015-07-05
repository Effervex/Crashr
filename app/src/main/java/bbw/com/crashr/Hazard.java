package bbw.com.crashr;

/**
 * Created by Sam on 4/07/2015.
 */
public class Hazard {
    private String hazardText_;

    public Hazard(String hazardText) {
        hazardText_ = hazardText;
    }

    @Override
    public String toString() {
        return hazardText_;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Hazard))
            return false;
        if (!hazardText_.equals(((Hazard) o).hazardText_))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return hazardText_.hashCode();
    }

    public String getText() {
        return hazardText_;
    }

    public void setText(String text) {
        this.hazardText_ = text;
    }
}
