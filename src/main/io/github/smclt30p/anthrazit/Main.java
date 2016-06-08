package io.github.smclt30p.anthrazit;

/*
 * Copyright (C) 2016  Ognjen Galić (smclt30p@gmail.com)
 * 
 * This file is part of Anthrazit.
 * 
 * Anthrazit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, up to version 2 of the License.
 * 
 * Anthrazit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Anthrazit. If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.IOException;
import java.io.FileOutputStream;

public class Main {
    public static void main(String[] args) {
        Logger log = Logger.getLogger(); 
        log.init("/home/gala", "anthrazit", true, true);

        try {
            FileOutputStream out = new FileOutputStream("/");
        } catch (IOException e) {
            log.catchException(e);
        }
    }
}
