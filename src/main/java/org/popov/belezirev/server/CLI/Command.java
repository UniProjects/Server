package org.popov.belezirev.server.CLI;

import java.util.List;

public abstract class Command {
	private String name;
	private List<String> arguments;

	public Command(String name, List<String> arguments) {
		this.name = name;
		this.arguments = arguments;
	}

	abstract protected void execute();
}
