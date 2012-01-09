package org.openengsb.domain.scm;

import java.util.List;

import org.openengsb.core.api.Event;

public class UpdateEvent extends Event {
    private List<CommitRef> changes;

    public UpdateEvent(List<CommitRef> changes) {
        this.changes = changes;
    }

    public void setChanges(List<CommitRef> changes) {
        this.changes = changes;
    }

    public List<CommitRef> getChanges() {
        return changes;
    }
}
