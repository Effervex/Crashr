package bbw.com.crashr;

/**
 * Created by Sam on 4/07/2015.
 */
public class Hazard {
    private String hazardText_;
    private String hazardType_;

    public Hazard(String hazardText, String hazardType) {
        hazardText_ = hazardText;
        hazardType_ = hazardType;
    }

    @Override
    public String toString() {
        return hazardText_;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Hazard))
            return false;
        if (!hazardType_.equals(((Hazard) o).hazardType_))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return hazardType_.hashCode();
    }

    public String getText() {
        return hazardText_;
    }

    public void setText(String text) {
        this.hazardText_ = text;
    }
}
