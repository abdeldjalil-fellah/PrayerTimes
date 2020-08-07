package jalil.prayertimes;

public interface IAsyncCompleted {

    void onVersion(String result);

    void onLocation(String[] result);

    void onHijriAutoUpdated(int hijriMethod, int hijriAdjust);

    void onHijriUploaded();
}
