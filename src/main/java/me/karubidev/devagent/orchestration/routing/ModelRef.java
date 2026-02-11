package me.karubidev.devagent.orchestration.routing;

public record ModelRef(String provider, String model) {

	public static ModelRef parse(String raw) {
		if (raw == null || raw.isBlank()) {
			throw new IllegalArgumentException("Model reference is blank");
		}
		int sep = raw.indexOf(':');
		if (sep <= 0 || sep == raw.length() - 1) {
			throw new IllegalArgumentException("Model reference format must be provider:model");
		}
		String provider = raw.substring(0, sep).trim();
		String model = raw.substring(sep + 1).trim();
		if (provider.isEmpty() || model.isEmpty()) {
			throw new IllegalArgumentException("Model reference format must be provider:model");
		}
		return new ModelRef(provider, model);
	}

	public String value() {
		return provider + ":" + model;
	}
}
