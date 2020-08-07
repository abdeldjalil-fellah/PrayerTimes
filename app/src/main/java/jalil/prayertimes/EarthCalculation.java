package jalil.prayertimes;

import net.sf.geographiclib.Constants;
import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;

class EarthCalculation {

    static final int EARTH_MODEL_WGS84 = 0;
    static final int EARTH_MODEL_SPHERE = 1;

    private static final double KAABA_LATITUDE = 21.422487;
    private static final double KAABA_LONGITUDE = 39.826206;

    private static final double EARTH_MEAN_RADIUS_IUGG = Constants.WGS84_a * (1 - Constants.WGS84_f / 3.0); // ≈ 6371008.8D;

    static Geodesic geodesic(int model) {

        switch (model) {
            default:
            case EARTH_MODEL_WGS84:
                return Geodesic.WGS84;
            case EARTH_MODEL_SPHERE:
                return new Geodesic(EARTH_MEAN_RADIUS_IUGG, 0);
        }
    }

    static double azimuthDegrees(int model, double latitudeFrom, double longitudeFrom, double latitudeTo, double longitudeTo) {

        return (geodesic(model).Inverse(latitudeFrom, longitudeFrom, latitudeTo, longitudeTo).azi1 + 360) % 360;
    }

    static double distanceMeters(int model, double latitudeFrom, double longitudeFrom, double latitudeTo, double longitudeTo) {

        return geodesic(model).Inverse(latitudeFrom, longitudeFrom, latitudeTo, longitudeTo).s12;
    }

    static double qiblaDegrees(int model, double latitude, double longitude) {

        return azimuthDegrees(model, latitude, longitude, KAABA_LATITUDE, KAABA_LONGITUDE);
    }

    static double[] nextPoint(int model, double latitude, double longitude, double heading, double distance) {

        GeodesicData geodesicData = geodesic(model).Direct(latitude, longitude, heading, distance);

        return new double[]{geodesicData.lat2, geodesicData.lon2};
    }
}
