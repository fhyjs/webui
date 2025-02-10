package org.eu.hanana.reimu.webui.session;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.core.util.UuidUtil;

import java.util.UUID;

@RequiredArgsConstructor
public class User {
    public final UUID uuid;
    public final JsonObject data=new JsonObject();;
    public User(){
        this.uuid= UuidUtil.getTimeBasedUuid();
    }
    @Getter
    protected boolean dirty = false;
    public void markDirty(){
        dirty=true;
    }
}
