package be.ucll.campus.campus_app.dto;

public class ReservatieRequestDTO {
    private String startTijd;
    private String eindTijd;
    private String commentaar;

    public String getStartTijd() {
        return startTijd;
    }

    public void setStartTijd(String startTijd) {
        this.startTijd = startTijd;
    }

    public String getEindTijd() {
        return eindTijd;
    }

    public void setEindTijd(String eindTijd) {
        this.eindTijd = eindTijd;
    }

    public String getCommentaar() {
        return commentaar;
    }

    public void setCommentaar(String commentaar) {
        this.commentaar = commentaar;
    }
}
