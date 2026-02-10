package Collectable;

public class CollectableFactoryProvider {
    public static CollectableFactory getFactory(CollectableType type) {
        switch (type) {
            case APPLE:
                return new AppleFactory();
            case SHEEP:
                return new SheepFactory();
            default:
                throw new IllegalArgumentException("Unknown collectable type: " + type);
        }
    }
}