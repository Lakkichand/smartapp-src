package com.youle.gamebox.ui.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by Administrator on 14-6-6.
 */
@Target(
        {FIELD})
@Retention(RUNTIME)
public @interface NoteParam {
}
