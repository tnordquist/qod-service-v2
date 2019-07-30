package edu.cnm.deepdive.qod.service;

import java.util.Random;
import org.springframework.stereotype.Component;

/**
 * Spring Bean extending {@link Random}, to support injection into other components requiring simple
 * (non-cryptographic and non-scientific quality) psuedorandom numbers.
 */
@Component
public class RngService extends Random {

  /**
   * Initializes this instance with the default seed strategy.
   */
  public RngService() {
  }

  /**
   * Initializes this instance with the specified seed value.
   *
   * @param seed PRNG seed value.
   */
  public RngService(long seed) {
    super(seed);
  }

}
