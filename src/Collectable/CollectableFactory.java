package Collectable;

public interface CollectableFactory {
    Collectable createCollectable(int x, int y);
    CollectableManager createManager();
}