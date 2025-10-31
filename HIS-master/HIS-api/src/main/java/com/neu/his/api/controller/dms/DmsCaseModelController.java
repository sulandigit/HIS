/**
 * Medical Record Template Management Controller
 * This controller handles operations for managing medical record templates and catalogs,
 * including creation, retrieval, updating, and deletion of templates and their directory structures.
 *
 * @author 赵煜
 * @version 1.0
 */
package com.neu.his.api.controller.dms;

import com.neu.his.common.api.CommonResult;
import com.neu.his.common.dto.dms.*;
import com.neu.his.dms.service.DmsCaseModelService;
import com.neu.his.mbg.model.DmsCaseModel;
import com.neu.his.mbg.model.DmsCaseModelCatalog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Api(tags = "DmsCaseModelController", description = "病历模板管理")
@RequestMapping("/DmsCaseModel")
@CrossOrigin(allowCredentials = "true")
public class DmsCaseModelController {
    /**
     * Medical record template service for business logic processing
     */
    @Autowired
    DmsCaseModelService dmsCaseModelService;

    /**
     * Create a new medical record template catalog or template
     * Parent ID must be provided when adding a new item
     *
     * @param dmsCaseModelOrCatalogParam The parameter object containing template or catalog information
     * @param result Binding result for validation
     * @return CommonResult with creation count if successful, failure result otherwise
     * @author 赵煜
     */
    @ApiOperation(value = "新增病历模板目录或模板")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult create(@RequestBody DmsCaseModelOrCatalogParam dmsCaseModelOrCatalogParam, BindingResult result) {
        int count = dmsCaseModelService.createCatOrModel(dmsCaseModelOrCatalogParam);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    /**
     * Display medical record template catalog tree
     * Merges personal, department, and hospital-wide template queries
     *
     * @param ownId Owner ID (staff ID, department ID, or hospital ID)
     * @param scope Scope of the templates (1: personal, 2: department, 3: hospital-wide)
     * @return CommonResult containing list of template catalog tree nodes
     */
    @ApiOperation(value = "显示病历模板目录树")
    @RequestMapping(value = "/listModelCatTree", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<List<DmsCaseModelCatalogNode>> listModelCatTree(@RequestParam Long ownId,@RequestParam Integer scope) {
        List<DmsCaseModelCatalogNode> dmsCaseModelCatalogTree = dmsCaseModelService.listModelCatTree(ownId,scope);
            return CommonResult.success(dmsCaseModelCatalogTree);
    }



    /**
     * Delete a medical record template or catalog
     *
     * @param id The ID of the template or catalog to be deleted
     * @return CommonResult with deletion count if successful, failure result otherwise
     */
    @ApiOperation(value = "删除病历模版或目录")
    @RequestMapping(value = "/deleteModelOrCat", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult deleteModelOrCat(@RequestParam("id") Long id){
        int count = dmsCaseModelService.deleteModelOrCat(id);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }


    /**
     * Update medical record template information
     *
     * @param modelId The ID of the template to be updated
     * @param dmsCaseModel The updated template object containing new information
     * @param result Binding result for validation
     * @return CommonResult with update count if successful, failure result otherwise
     */
    @ApiOperation(value = "更新病历模版信息")
    @RequestMapping(value = "/updateModel", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateModel(@RequestParam("modelId") Long modelId, @RequestBody DmsCaseModel dmsCaseModel, BindingResult result){
        int count = dmsCaseModelService.updateModel(modelId,dmsCaseModel);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }


    /**
     * Update the name of a catalog or template
     *
     * @param id The ID of the catalog or template
     * @param name The new name to be set
     * @return CommonResult with update count if successful, failure result otherwise
     */
    @ApiOperation(value = "更新目录或模板的名字")
    @RequestMapping(value = "/updateModel/{id}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult updateName(@PathVariable("id") Long id,@RequestParam("name")String name){
        int count = dmsCaseModelService.updateName(id,name);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }


    /**
     * Query detailed information of a template by template ID
     *
     * @param id The ID of the template to query
     * @return CommonResult containing the detailed template information if found, failure result otherwise
     */
    @ApiOperation(value = "根据模板id查询模板详细信息")
    @RequestMapping(value = "/getModelDetail/{id}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<DmsCaseModel> getModelDetail(@PathVariable("id") Long id){
        DmsCaseModel dmsCaseModel = dmsCaseModelService.getModelDetail(id);
        if(null!=dmsCaseModel){
            return CommonResult.success(dmsCaseModel);
        }else{
            return CommonResult.failed("查询模板详细信息失败");
        }
    }

    /**
     * Get all medical record templates by staff ID
     * Retrieves all templates accessible to the specified staff member
     *
     * @param staffId The ID of the staff member
     * @return CommonResult containing the list of all available templates for the staff
     */
    @ApiOperation(value = "根据staffId获取所有病历模板")
    @RequestMapping(value = "/getAllStaffModel", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<DmsCaseModelListResult> getAllStaffModel(@RequestParam("staffId") Long staffId){
        return CommonResult.success(dmsCaseModelService.getAllStaffModel(staffId));
    }

}
