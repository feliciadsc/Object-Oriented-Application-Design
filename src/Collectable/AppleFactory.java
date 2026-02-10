package Collectable;

public class AppleFactory implements CollectableFactory {
    @Override
    public Collectable createCollectable(int x, int y) {
        return new Apple(x, y);
    }

    @Override
    public CollectableManager createManager() {
        return new AppleManager();
    }
}