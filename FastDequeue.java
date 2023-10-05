import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import exceptions.EmptyPCollectionException;
import exceptions.NotFoundException;
import interfaces.DequeueI;

public class FastDequeue<E> implements DequeueI<E> {
    /**
     * This in an implementation of a dynamically resizable dequeue.
     * All operations run in amortized constant time.
     * <p>
     * The general idea is to use an array to store the dequeue elements.
     * The array provides O(1) access to any position but is of fixed capacity.
     * So we resize the array by doubling its size when it's full.
     * <p>
     * We will maintain two pointers into the array: a 'right' pointer where
     * enqueue/dequeueBack operations are performed and a 'left' pointer where
     * enqueueBack/dequeue operations are performed.
     * <p>
     * The right and left pointers move around the array as elements are inserted
     * and deleted. In the general situation, the values are stored in the array as follows:
     * <p>
     * |-------------------------|
     * | 4 5 6 _ _ _ _ _ _ 1 2 3 |
     * |-------------------------|
     * /\        /\      /\
     * left      right  capacity
     * <p>
     * left and right typically point to the next available slot
     * all arithmetic modulo capacity
     * data stored at right+1, right+2, ... left-2, left-1
     */
    private int capacity;
    private Optional<E>[] elements;
    private int left, right, size;
    private final Function<Integer, Integer> growthStrategy;

    @SuppressWarnings("unchecked")
    FastDequeue() {
        this.capacity = 8;
        this.elements = (Optional<E>[]) Array.newInstance(Optional.class, capacity);
        Arrays.fill(elements, Optional.empty());
        left = 0;
        right = this.capacity - 1;
        size = 0;
        this.growthStrategy = n -> n * 2;
    }

    public int size() { // O(1)
        return this.size;
    }

    public boolean isEmpty() { // O(1)
        return this.size == 0;
    }

    public void enqueue(E item) { // uses right pointer; O(1) amortized
        if (this.size == this.capacity) {
            this.resize();
        } else if (this.right == this.capacity - 1) {
            this.right = 0;
        } else {
            this.right++;
        }
        this.elements[this.right] = Optional.of(item);
        this.size++;
    }

    public E dequeue() throws EmptyPCollectionException { // uses left pointer; O(1) amortized
        Optional<E> element = this.elements[this.left];
        if (element.isPresent()) {
            E elem = element.get();
            this.elements[this.left] = Optional.empty();
            this.left = Math.floorMod(this.left + 1, this.capacity);
            this.size--;
            return elem;
        } else {
            throw new EmptyPCollectionException();
        }
    }
    public void enqueueBack(E item) { // uses left pointer; O(1) amortized
        if (this.size == this.capacity) {
            this.resize();
        } else if (this.left == 0) {
            this.left = Math.floorMod(this.left - 1, this.capacity);
        } else {
            this.left--;
        }
        this.elements[this.left] = Optional.of(item);
        this.size++;
    }

    public E dequeueBack() throws EmptyPCollectionException { // uses right pointer; O(1) amortized
        Optional<E> element = this.elements[this.right];
        if (element.isPresent()) {
            E elem = element.get();
            this.elements[this.right] = Optional.empty();
            this.right = Math.floorMod(this.right - 1, this.capacity);
            this.size--;
            return elem;
        } else {
            throw new EmptyPCollectionException();
        }
    }

    /**
     * Here is an example of how resize should work:
     * <p>
     * Current queue:
     * <p>
     * 0 1 2 3 4 5
     * |-------------|
     * | 4 5 6 1 2 3 |
     * |-------------|
     * right = 2; left = 3; capacity = 6
     * <p>
     * After resize:
     * 0 1 2 3 4 5 6 7 8 9 10 11
     * |--------------------------|
     * | 1 2 3 4 5 6 _ _ _ _ _ _  |
     * |--------------------------|
     * right = 11; left = 6; capacity = 12
     */
    private void resize() {
        int newCapacity = this.growthStrategy.apply(this.capacity);
        Optional<E>[] newElements = (Optional<E>[]) Array.newInstance(Optional.class, newCapacity);
        Arrays.fill(newElements, Optional.empty());
        int newLeft = newCapacity - this.size;
        int newRight = Math.floorMod(newLeft + this.size - 1, newCapacity);

        // Copy elements from the old array to the new array
        for (int i = 0; i < this.size; i++) {
            newElements[newRight] = this.elements[this.left];
            newRight = Math.floorMod(newRight - 1, newCapacity);
            this.left = Math.floorMod(this.left + 1, this.capacity);
        }

        this.capacity = newCapacity;
        this.elements = newElements;
        this.left = newLeft;
        this.right = Math.floorMod(newLeft + this.size - 1, newCapacity);
    }
}