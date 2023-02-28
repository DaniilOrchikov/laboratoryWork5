package command;

import ticket.Ticket;

public class CommandWithTicket extends Command {
    private final Ticket t;

    public CommandWithTicket(String[] command, Ticket t) {
        super(command);
        this.t = t;
    }

    public Ticket getTicket() {
        return t;
    }
}
