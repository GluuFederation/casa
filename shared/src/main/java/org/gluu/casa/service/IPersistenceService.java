package org.gluu.casa.service;

import org.gluu.search.filter.Filter;

import java.util.List;

/**
 * Provides CRUD access to the underlying persistence engine of you Gluu Server installation. Starting with Casa 4.0 this
 * interface is the mechanism of choice for interacting with data.
 * <p>This interface resembles older <code>ILdapService</code> so developers with previous acquaintance can start coding quickly,
 * however classes/instances passed to these methods are supposed to use the <code>oxCore</code> annotations found in package
 * <code>org.gluu.persist.annotation</code> in lieu of <code>com.unboundid.ldap.sdk.persist</code>
 * annotations of UnboundID LDAP SDK.</p>
 * <p>To obtain an instance object that implements this interface, use method
 * {@link org.gluu.casa.misc.Utils#managedBean(Class)}.</p>
 * @author jgomer
 */
public interface IPersistenceService extends LocalDirectoryInfo2 {

    /**
     * Builds a {@link List} of objects of type <code>T</code> from a search (with scope of SUB) using <code>baseDn</code>
     * as search base; this type of search accounts for the entry referenced at <code>baseDn</code> and any subordinate
     * entries to any depth. The search can use an instance of <code>org.gluu.search.filter.Filter</code> to include an
     * LDAP-like filter expression.
     * <p>Note this search is performed in the context of <code>oxcore-persist</code>. In this sense, the Class
     * referenced as parameter has to be annotated with <code>org.gluu.persist.model.base.Entry</code>
     * and potentially other annotations of the same package to be functional.</p>
     * @param clazz A class to which the search objects must belong to
     * @param baseDn Search base DN
     * @param filter Filter to constrain the search (supply null to returned ALL entries under the base DN that can be
     *               associated to Class clazz)
     * @param <T> Type parameter of clazz
     * @return A List of matching objects. Empty if no matches
     */
    <T> List<T> find(Class<T> clazz, String baseDn, Filter filter);

    /**
     * Performs a search as method {@link #find(Class, String, Filter)} does except the first <code>start</code> elements
     * are skipped and at most <code>count</code> results are included in the returned list
     * @param clazz A class to which the search objects must belong to
     * @param baseDn Search base DN
     * @param filter Filter to constrain the search (supply null to returned ALL entries under the base DN that can be
     *               associated to Class clazz)
     * @param start Zero-based index at which the search starts
     * @param count Maximum number of results to return
     * @param <T> Type parameter of clazz
     * @return A List of matching objects according to the rules specified above. Empty if no matches
     */
    <T> List<T> find(Class<T> clazz, String baseDn, Filter filter, int start, int count);

    /**
     * Builds a {@link List} of objects of type <code>T</code> from a search (with scope of SUB) using as search base
     * the field annotated with <code>org.gluu.persist.annotation.DN</code> in the object passed as parameter;
     * this type of search accounts for the entry referenced at the search base and any subordinate
     * entries to any depth. The object passed as parameter is used to internally build a filter to perform the search.
     * <p>Note this search is performed in the context of  <code>oxcore-persist</code>. In this sense, the class to which
     * the object passed belongs to has to be annotated with <code>org.gluu.persist.model.base.Entry</code>
     * and potentially other annotations of the same package to be functional.</p>
     * @param object An object employed to build a filter
     * @param <T> Type parameter of clazz
     * @return A List of matching objects. Empty if no matches
     */
    <T> List<T> find(T object);

    /**
     * Similar to {@link #find(Object)} search except it only returns the amount of matching objects.
     * @param object Object describing the search
     * @param <T> Type parameter of the class to which the object belongs to
     * @return An int with the number of matching objects, -1 if there was an error performing the operation
     */
    <T> int count(T object);

    /**
     * Adds an entry in the underlying persistence engine.
     * @param object Represents the entry to be added. Its class should be annotated with
     *               <code>org.gluu.persist.model.base.Entry</code>
     * @param <T> Type parameter of clazz
     * @return A boolean value indicating the success (true) or failure (false) of the operation
     */
    <T> boolean add(T object);

    /**
     * Retrieves the object representing the entry at the provided DN location.
     * @param clazz The class the object to return is an instance of
     * @param dn Path (DN) of the entry
     * @param <T> Type parameter of clazz
     * @return An object of type T (null if an error was found performing the operation or if the entry does not exist)
     */
    <T> T get(Class<T> clazz, String dn);

    /**
     * Stores a previously obtained object (via get or find) - and potentially modified - into the persistence engine.
     * @param object An object whose attributes are potentially altered (eg. via setters) with respect to the original
     *              retrieved via get or find methods
     * @param <T> Type parameter of the class to which the object belongs to
     * @return A boolean value indicating the success (true) or failure (false) of the operation.
     */
    <T> boolean modify(T object);

    /**
     * Deletes the entry represented by parameter <code>object</code> from the persistence engine.
     * @param object A representation of the object to delete. It must have been previously retrieved via get or find methods
     * @param <T> Type parameter of the class to which the object belongs to
     * @return A boolean value indicating the success (true) or failure (false) of the operation
     */
    <T> boolean delete(T object);

    /**
     * Initializing of the Persistence.
     * @return result of initializing
     */
    boolean initialize();
}
