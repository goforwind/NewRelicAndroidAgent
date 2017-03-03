// 
// Decompiled by Procyon v0.5.30
// 

package proguard.canary;

public class NewRelicCanary
{
    private String sound;
    
    public NewRelicCanary(final String sound) {
        this.sound = sound;
    }
    
    public static void canaryMethod() {
        final NewRelicCanary canary = new NewRelicCanary("tweet!");
    }
}
