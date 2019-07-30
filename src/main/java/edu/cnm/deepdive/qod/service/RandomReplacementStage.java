package edu.cnm.deepdive.qod.service;

import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RandomReplacementStage<T> {

  private final Random rng;
  private int counter;
  private T candidate;

  @Autowired
  public RandomReplacementStage(Random rng) {
    this.rng = rng;
    reset();
  }

  public T getCandidate() {
    return candidate;
  }

  public T offer(T candidate) {
    if (rng.nextInt(++counter) == 0) {
      this.candidate = candidate;
    }
    return this.candidate;
  }

  public void reset() {
    counter = 0;
    candidate = null;
  }

  public void reset(long seed) {
    rng.setSeed(seed);
    reset();
  }

}
