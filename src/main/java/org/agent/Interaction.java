package org.agent;

// Interaction 类，包含交互类型属性
public class Interaction {
    private String interactionType; // 交互类型

    // 构造函数
    public Interaction(String interactionType) {
        this.interactionType = interactionType;
    }

    // 智能体之间进行交互的方法
    public void interact() {
        System.out.println("智能体正在进行 " + interactionType + " 交互...");
    }
}