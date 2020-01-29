/**
 * Simplified C binding for hnswlib able to work with JNA (Java Native Access)
 * in order to get a similar native performance on Java. This code is based on the python 
 * binding available at: https://github.com/nmslib/hnswlib/blob/master/python_bindings/bindings.cpp
 * 
 * Some modifications and simplifications have been done on the C side. 
 * The multithread support can be used and handled on the Java side.
 * 
 * This work is still in progress. Please feel free to contribute and give ideas.
 */

#include <iostream>
#include "hnswlib/hnswlib.h"
#include <thread>
#include <atomic>

#if _WIN32
#define DLLEXPORT __declspec(dllexport)
#else
#define DLLEXPORT
#endif
#define EXTERN_C DLLEXPORT extern "C"

#define RESULT_SUCCESSFUL 0
#define RESULT_EXCEPTION_THROWN 1
#define RESULT_INDEX_ALREADY_INITIALIZED 2
#define RESULT_QUERY_CANNOT_RETURN 3
#define RESULT_ITEM_CANNOT_BE_INSERTED_INTO_THE_VECTOR_SPACE 4
#define TRY_CATCH_RETURN_INT_BLOCK(block)   int resultCode = RESULT_SUCCESSFUL; try { block } catch (...) { resultCode = RESULT_EXCEPTION_THROWN; }; return resultCode;

template<typename dist_t, typename data_t=float>
class Index {
public:
    Index(const std::string &space_name, const int dim) :
            space_name(space_name), dim(dim) {
        data_must_be_normalized = false;
        if(space_name=="L2") {
            l2space = new hnswlib::L2Space(dim);
        } else if(space_name=="IP") {
            l2space = new hnswlib::InnerProductSpace(dim);
        } else if(space_name=="COSINE") {
            l2space = new hnswlib::InnerProductSpace(dim);
            data_must_be_normalized = true;
        }
        appr_alg = NULL;
        ep_added = true;
        index_inited = false;
    }

    int init_new_index(const size_t maxElements, const size_t M, const size_t efConstruction, const size_t random_seed) {
        TRY_CATCH_RETURN_INT_BLOCK({
            if (appr_alg) {
                return RESULT_INDEX_ALREADY_INITIALIZED;
            }
            appr_alg = new hnswlib::HierarchicalNSW<dist_t>(l2space, maxElements, M, efConstruction, random_seed);
            index_inited = true;
            ep_added = false;
        });
    }

    void set_ef(size_t ef) {
        appr_alg->ef_ = ef;
    }

    size_t get_ef_construction() {
        return appr_alg->ef_construction_;
    }

    size_t get_M() {
        return appr_alg->M_;
    }

    int save_index(const std::string &path_to_index) {
        TRY_CATCH_RETURN_INT_BLOCK({
            appr_alg->saveIndex(path_to_index);
        });
    }

    int load_index(const std::string &path_to_index, size_t max_elements) {
        TRY_CATCH_RETURN_INT_BLOCK({
            if (appr_alg) {
                std::cerr << "Warning: Calling load_index for an already inited index. Old index is being deallocated.";
                delete appr_alg;
            }
            appr_alg = new hnswlib::HierarchicalNSW<dist_t>(l2space, path_to_index, false, max_elements);
        });
    }

	void normalize_array(float* array){
        float norm = 0.0f;
        for (int i=0; i<dim; i++) {
            norm += (array[i] * array[i]);
        }
        norm = 1.0f / (sqrtf(norm) + 1e-30f);
        for (int i=0; i<dim; i++) {
            array[i] = array[i] * norm;
        }
    }

    int add_item(float* item, bool item_normalized, int id) {
        TRY_CATCH_RETURN_INT_BLOCK({
            if (get_current_count() >= get_max_elements()) {
                return RESULT_ITEM_CANNOT_BE_INSERTED_INTO_THE_VECTOR_SPACE;
            }            
            if ((data_must_be_normalized == true) && (item_normalized == false)) {
                normalize_array(item);                
            }
            int current_id = id != -1 ? id : incremental_id++;             
            appr_alg->addPoint(item, current_id);                
        });
    }

    int knn_query(float* input, bool input_normalized, int k, int* indices /* output */, float* coefficients /* output */) {
        std::priority_queue<std::pair<dist_t, hnswlib::labeltype >> result;
        TRY_CATCH_RETURN_INT_BLOCK({
            if ((data_must_be_normalized == true) && (input_normalized == false)) {
                normalize_array(input);
            }
            result = appr_alg->searchKnn((void*) input, k);
            if (result.size() != k)
                return RESULT_QUERY_CANNOT_RETURN;
            for (int i = k - 1; i >= 0; i--) {
                auto &result_tuple = result.top();
                coefficients[i] = result_tuple.first;
                indices[i] = result_tuple.second;
                result.pop();
            }       
        });
    }

    void mark_deleted(size_t label) {
        appr_alg->markDelete(label);
    }

    void resize_index(size_t new_size) {
        appr_alg->resizeIndex(new_size);
    }

    size_t get_max_elements() const {
        return appr_alg->max_elements_;
    }

    size_t get_current_count() const {
        return appr_alg->cur_element_count;
    }

    std::string space_name;
    int dim;
    bool index_inited;
    bool ep_added;
    bool data_must_be_normalized;
    std::atomic<unsigned long> incremental_id{1};
    hnswlib::HierarchicalNSW<dist_t> *appr_alg;
    hnswlib::SpaceInterface<float> *l2space;

    ~Index() {
        delete l2space;
        if (appr_alg)
            delete appr_alg;
    }
};

EXTERN_C Index<float>* createNewIndex(char* spaceName, int dimension){
    Index<float> *object = new Index<float>(spaceName, dimension);
    return object;
}

EXTERN_C int initNewIndex(Index<float>* index, int maxNumberOfElements, int M = 16, int efConstruction = 200, int randomSeed = 100) {
    return index->init_new_index(maxNumberOfElements, M, efConstruction, randomSeed);
} 

EXTERN_C int addItemToIndex(float* item, int normalized, int id, Index<float>* index) { 
    return index->add_item(item, normalized, id);
}

EXTERN_C int getIndexLength(Index<float>* index) {
    if (index->appr_alg) {
        return index->appr_alg->cur_element_count;
    } else {
        return 0;
    }
}

EXTERN_C int saveIndexToPath(Index<float>* index, char* path) {
    std::string path_string(path);
    return index->save_index(path_string);
}

EXTERN_C int loadIndexFromPath(Index<float>* index, size_t maxNumberOfElements, char* path) {
    std::string path_string(path);
    return index->load_index(path_string, maxNumberOfElements);
}

EXTERN_C int knnQuery(Index<float>* index, float* input, int normalized, int k, int* indices /* output */, float* coefficients /* output */) {
    return index->knn_query(input, normalized, k, indices, coefficients);
}

EXTERN_C int clearIndex(Index<float>* index) {
    index->~Index();
    return 0;
}

int main(){
    return 0;
}