package org.myeslib.gson;

import lombok.AllArgsConstructor;

import org.myeslib.data.AggregateRootHistory;

import com.google.common.base.Function;
import com.google.gson.Gson;

@AllArgsConstructor
public class ToStringFunction implements Function<AggregateRootHistory, String>{
	private final Gson gson;
	@Override
	public String apply(AggregateRootHistory input) {
		return gson.toJson(input);
	}

}
