package com.smartsparrow.export.route;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

public class NewestExchangeAggregationStrategy implements AggregationStrategy {
    @Override

    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
          if (newExchange != null) {
              return newExchange;
          }
          return oldExchange;
    }
}

