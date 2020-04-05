package sns.nv.beans;

import lombok.Getter;

public final class IntentVertex implements Comparable<IntentVertex> {

    @Getter
    private String name;
    @Getter
    private String action;
    @Getter
    private IntentBean intent;

    private IntentVertex() {}

    public IntentVertex(IntentBean intentBean) {
        this.name = intentBean.getName();
        this.action = intentBean.getResponses().stream()
            .findFirst()
            .map(IntentBean.Response::getAction)
            .filter(action -> action != null)
            .orElse("-no action-");
        this.intent = intentBean;
    }

    @Override
    public String toString() {
        return name + "\n" + action;
    }

    @Override
    public int hashCode() {
        return (name + "\n" + action).hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof IntentVertex) && (this.toString().equals(o.toString()));
    }

    @Override
    public int compareTo(IntentVertex o) {
        return this.name.compareTo(o.getName());
    }

}
