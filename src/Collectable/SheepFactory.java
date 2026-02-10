package Collectable;

public class SheepFactory implements CollectableFactory {
    @Override
    public Collectable createCollectable(int x, int y) {
        return new Sheep(x, y);
    }

    @Override
    public CollectableManager createManager() {
        return new SheepManager();
    }
}