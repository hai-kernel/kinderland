package kinderland.common.seed;

public interface DataSeeder {
    void seed();
    String getName();
    
    default boolean isEnabled() {
        return true;
    }
}
