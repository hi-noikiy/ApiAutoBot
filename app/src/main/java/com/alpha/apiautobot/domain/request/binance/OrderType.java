package com.alpha.apiautobot.domain.request.binance;

/**
 * Type of order to submit to the system.
 */
public enum OrderType {
  LIMIT,
  MARKET,
  STOP_LOSS,
  STOP_LOSS_LIMIT,
  TAKE_PROFIT,
  TAKE_PROFIT_LIMIT,
  LIMIT_MAKER
}
