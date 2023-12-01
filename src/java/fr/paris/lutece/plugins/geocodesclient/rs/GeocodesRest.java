/*
 * Copyright (c) 2002-2023, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */

package fr.paris.lutece.plugins.geocodesclient.rs;

import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import fr.paris.lutece.plugins.geocode.v1.web.rs.dto.City;
import fr.paris.lutece.plugins.geocode.v1.web.rs.dto.Country;
import fr.paris.lutece.plugins.geocode.v1.web.service.GeoCodeService;
import fr.paris.lutece.plugins.rest.service.RestConstants;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.date.DateUtil;
import fr.paris.lutece.util.json.ErrorJsonResponse;
import fr.paris.lutece.util.json.JsonResponse;
import fr.paris.lutece.util.json.JsonUtil;

/**
 * CityRest
 */
@Path( RestConstants.BASE_PATH + Constants.API_PATH + Constants.VERSION_PATH  )
public class GeocodesRest
{
	private static final int VERSION_1 = 1;
	private static final String GEOCODE_BEAN_NAME = "geocodes.geoCodesService";
    private GeoCodeService _geoCodesService;
    		
    /**
     * Get City List with date
     * @param nVersion the API version
     * @param strVal the search string
     * @param dateRef the reference date
     * @return the City List
     */
    @GET
    @Path( Constants.CITY_PATH )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getCityListByDate( @PathParam( Constants.VERSION ) Integer nVersion,
    							@QueryParam( Constants.SEARCHED_STRING ) String strVal,
    							@QueryParam( Constants.ADDITIONAL_PARAM ) String strDateRef ) 
    {
        if ( nVersion == VERSION_1 )
        {
        	DateFormat formatter = new SimpleDateFormat("yyyy-MM-DD"); 
        	Date dateref = new Date( );
			try {
				dateref = (Date)formatter.parse( strDateRef );
			} catch (ParseException e) {
				AppLogService.error( Constants.ERROR_FORMAT_DATE_RESOURCE );
	            return Response.status( Response.Status.NOT_FOUND )
	                    .entity( JsonUtil.buildJsonResponse( new ErrorJsonResponse( Response.Status.NOT_FOUND.name( ), Constants.ERROR_FORMAT_DATE_RESOURCE ) ) )
	                    .build( );
			}
        	return getCityListByNameAndDateV1( strVal, dateref);
        }
        AppLogService.error( Constants.ERROR_NOT_FOUND_VERSION );
        return Response.status( Response.Status.NOT_FOUND )
                .entity( JsonUtil.buildJsonResponse( new ErrorJsonResponse( Response.Status.NOT_FOUND.name( ), Constants.ERROR_NOT_FOUND_VERSION ) ) )
                .build( );
    }
    
    /**
     * init geocode service
     */
    private void init()
    {
    	if ( _geoCodesService == null)
    	{
    		_geoCodesService = SpringContextService.getBean( GEOCODE_BEAN_NAME );
    	}
    }
    
    /**
     * set display values
     * 
     * @param lstCities
     */
    private void fillCitiesDisplayValues( List<City> lstCities )
    {
    	lstCities.stream( ).forEach( c -> c.setDisplayValue(  c.getValue() + " (" + c.getCodeZone() + ")" ) );
    }
    
    /**
     * Get City List V1
     * @return the City List for the version 1
     */
    private Response getCityListByNameAndDateV1( String strSearchBeginningVal, Date dateCity )
    {
    	List<City> lstCities = new ArrayList<>( );
    	
        if ( strSearchBeginningVal != null || strSearchBeginningVal.length( ) >= 3 )
        {
        	try {
        		init( );
    			lstCities = _geoCodesService.getListCitiesByNameAndDateLike( strSearchBeginningVal, dateCity );
    			fillCitiesDisplayValues( lstCities );
    		} catch (Exception e) {
    			AppLogService.error( e );
    		}
        }

        return Response.status( Response.Status.OK )
                .entity( JsonUtil.buildJsonResponse( new JsonResponse( lstCities ) ) )
                .build( );
    }
    
    /**
     * Search Countries
     * @param nVersion the API version
     * @param search the searched string
     * @param dateRef the reference date
     * @return the Countries
     */
    @GET
    @Path( Constants.COUNTRY_PATH )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getCountiesListByNameAndDate(
			    @PathParam( Constants.VERSION ) Integer nVersion,
			    @QueryParam( Constants.SEARCHED_STRING ) String strVal,
				@QueryParam( Constants.DATE ) String strDateRef )
    {
        if ( nVersion == VERSION_1 )
        {
        	DateFormat formatter = new SimpleDateFormat("yyyy-MM-DD"); 
        	Date dateref = new Date( );
			try {
				dateref = (Date)formatter.parse( strDateRef );
			} catch (ParseException e) {
				AppLogService.error( Constants.ERROR_FORMAT_DATE_RESOURCE );
	            return Response.status( Response.Status.NOT_FOUND )
	                    .entity( JsonUtil.buildJsonResponse( new ErrorJsonResponse( Response.Status.NOT_FOUND.name( ), Constants.ERROR_FORMAT_DATE_RESOURCE ) ) )
	                    .build( );
			}
        	return getCountriesListByNameAndDateV1( strVal, dateref );
        }
        
        AppLogService.error( Constants.ERROR_NOT_FOUND_VERSION );
        return Response.status( Response.Status.NOT_FOUND )
                .entity( JsonUtil.buildJsonResponse( new ErrorJsonResponse( Response.Status.NOT_FOUND.name( ), Constants.ERROR_NOT_FOUND_VERSION ) ) )
                .build( );
    }
    
    /**
     * Get Country V1
     * @param id the id
     * @return the Country for the version 1
     */
    private Response getCountriesListByNameAndDateV1( String strSearchBeginningVal, Date dateRef )
    {
    	List<Country> listCountries = new ArrayList<>();
    	
        if ( strSearchBeginningVal == null || strSearchBeginningVal.length( ) < 3 )
        {
        	try {
        		init( );
        		listCountries = _geoCodesService.getListCountryByNameAndDate( strSearchBeginningVal, dateRef );
    		} catch (Exception e) {
    			AppLogService.error( e );
    		}
        }
        
        return Response.status( Response.Status.OK )
                .entity( JsonUtil.buildJsonResponse( new JsonResponse( listCountries ) ) )
                .build( );
    }
}