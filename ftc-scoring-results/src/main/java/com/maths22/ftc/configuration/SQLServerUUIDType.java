package com.maths22.ftc.configuration;//TODO link to SQL server only

/*
 * This file is adapted from the below file and released under the same license
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
*/

import org.hibernate.annotations.TypeDef;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.java.UUIDTypeDescriptor;
import org.hibernate.type.descriptor.sql.BasicBinder;
import org.hibernate.type.descriptor.sql.BasicExtractor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

import javax.persistence.MappedSuperclass;
import java.sql.*;
import java.util.UUID;

/**
 * Specialized type mapping for {@link UUID} and the MS SQL unique identifier data type
 *
 * @author Steve Ebersole
 * @author David Driscoll
 * @author Jacob Burroughs
 */
@MappedSuperclass
@TypeDef(defaultForType = UUID.class, typeClass = SQLServerUUIDType.class)
public class SQLServerUUIDType extends AbstractSingleColumnStandardBasicType<UUID> {

    public static final SQLServerUUIDType INSTANCE = new SQLServerUUIDType();

    public SQLServerUUIDType() {
        super( SQLServerUUIDSqlTypeDescriptor.INSTANCE, UUIDTypeDescriptor.INSTANCE );
    }

    public String getName() {
        return "sqlsrv-uuid";
    }

    @Override
    protected boolean registerUnderJavaType() {
        // register this type under UUID when it is added to the basic type registry
        return true;
    }

    public static class SQLServerUUIDSqlTypeDescriptor implements SqlTypeDescriptor {
        public static final SQLServerUUIDSqlTypeDescriptor INSTANCE = new SQLServerUUIDSqlTypeDescriptor();

        public int getSqlType() {
            // ugh
            return Types.CHAR;
        }

        @Override
        public boolean canBeRemapped() {
            return true;
        }

        public <X> ValueBinder<X> getBinder(final JavaTypeDescriptor<X> javaTypeDescriptor) {
            return new BasicBinder<X>( javaTypeDescriptor, this ) {
                @Override
                protected void doBind(PreparedStatement st, X value, int index, WrapperOptions options) throws SQLException {
                    st.setObject( index, javaTypeDescriptor.unwrap( value, String.class, options ), getSqlType() );
                }

                @Override
                protected void doBind(CallableStatement st, X value, String name, WrapperOptions options)
                        throws SQLException {
                    st.setObject( name, javaTypeDescriptor.unwrap( value, String.class, options ), getSqlType() );
                }
            };
        }

        public <X> ValueExtractor<X> getExtractor(final JavaTypeDescriptor<X> javaTypeDescriptor) {
            return new BasicExtractor<X>( javaTypeDescriptor, this ) {
                @Override
                protected X doExtract(ResultSet rs, String name, WrapperOptions options) throws SQLException {
                    return javaTypeDescriptor.wrap( rs.getObject( name ), options );
                }

                @Override
                protected X doExtract(CallableStatement statement, int index, WrapperOptions options) throws SQLException {
                    return javaTypeDescriptor.wrap( statement.getObject( index ), options );
                }

                @Override
                protected X doExtract(CallableStatement statement, String name, WrapperOptions options) throws SQLException {
                    return javaTypeDescriptor.wrap( statement.getObject( name ), options );
                }
            };
        }
    }
}
