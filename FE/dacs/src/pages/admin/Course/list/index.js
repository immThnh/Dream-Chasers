import styles from "./List.module.scss";
import clsx from "clsx";
import { Link } from "react-router-dom";
import deleteIcon from "../../../../assets/images/delete.svg";
import viewIcon from "../../../../assets/images/view.svg";
import editIcon from "../../../../assets/images/edit.svg";
import { useEffect, useState } from "react";
import * as dataApi from "../../../../api/apiService/dataService";
import Select from "react-select";
import { toast } from "sonner";
import { data } from "autoprefixer";

function ListCourse() {
    const [courses, setCourses] = useState([]);
    const [options, setOptions] = useState([]);
    const handleRemoveCourse = (id) => {
        console.log(id);
        const fetchApi = async () => {
            toast.promise(dataApi.removeCourse(id), {
                loading: "Removing...",
                success: () => {
                    window.location.reload();
                    return "Remove successfully";
                },
                error: (error) => {
                    return error.content;
                },
            });
        };

        fetchApi();
    };

    const handleSelectChange = (e) => {
        const fetchApi = () => {
            toast.promise(dataApi.getCoursesByCategory(e.id), {
                loading: "loading...",
                success: (data) => {
                    setCourses(data.content);
                    return "Get data successfully";
                },
                error: (error) => {
                    return error;
                },
            });
        };

        const debounceApi = debounce(fetchApi);
        debounceApi();
    };

    const handleSearchInputChange = (e) => {
        const fetchApi = () => {
            toast.promise(dataApi.getCourseByName(e.target.value), {
                loading: "loading...",
                success: (data) => {
                    setCourses(data.content);
                    return "Get data successfully";
                },
                error: (error) => {
                    console.log(error);
                    return error;
                },
            });
        };
        const debounceApi = debounce(fetchApi, 1000);
        debounceApi();
    };
    let timerId;
    const debounce = (func, delay = 600) => {
        return () => {
            clearTimeout(timerId);
            timerId = setTimeout(() => {
                func();
            }, delay);
        };
    };

    useEffect(() => {
        const fetchApi = async () => {
            try {
                const result = await dataApi.getAllCourse();
                let categories = [];
                categories = await dataApi.getAllCategories();
                categories.push({ id: "-1", name: "All" });
                setCourses(result.content);
                setOptions(categories);
            } catch (error) {
                console.log(error.response);
            }
        };
        fetchApi();
    }, []);
    return (
        <div className="flex justify-center w-full ">
            <div className="container mt-5 mx-14">
                <div className="wrapMainDash">
                    <div className={clsx(styles.topMain)}>
                        <div className={clsx(styles.itemTopMain)}>
                            <h4>List</h4>
                        </div>
                        <div className={clsx(styles.itemTopMain)}>
                            <Link
                                to={"/admin/course/create"}
                                className={styles.btnCreate}
                            >
                                <svg
                                    xmlns="http://www.w3.org/2000/svg"
                                    aria-hidden="true"
                                    role="img"
                                    className="component-iconify MuiBox-root css-1t9pz9x iconify iconify--mingcute"
                                    width="20px"
                                    height="20px"
                                    viewBox="0 0 24 24"
                                >
                                    <g fill="none">
                                        <path d="M24 0v24H0V0zM12.593 23.258l-.011.002l-.071.035l-.02.004l-.014-.004l-.071-.035c-.01-.004-.019-.001-.024.005l-.004.01l-.017.428l.005.02l.01.013l.104.074l.015.004l.012-.004l.104-.074l.012-.016l.004-.017l-.017-.427c-.002-.01-.009-.017-.017-.018m.265-.113l-.013.002l-.185.093l-.01.01l-.003.011l.018.43l.005.012l.008.007l.201.093c.012.004.023 0 .029-.008l.004-.014l-.034-.614c-.003-.012-.01-.02-.02-.022m-.715.002a.023.023 0 0 0-.027.006l-.006.014l-.034.614c0 .012.007.02.017.024l.015-.002l.201-.093l.01-.008l.004-.011l.017-.43l-.003-.012l-.01-.01z"></path>
                                        <path
                                            fill="currentColor"
                                            d="M11 20a1 1 0 1 0 2 0v-7h7a1 1 0 1 0 0-2h-7V4a1 1 0 1 0-2 0v7H4a1 1 0 1 0 0 2h7z"
                                        ></path>
                                    </g>
                                </svg>
                                New Course
                            </Link>
                        </div>
                    </div>

                    <div className="formGroup flex flex-col gap-3">
                        <div
                            className={clsx(
                                styles.contentMain,
                                "flex justify-between"
                            )}
                        >
                            <div className={clsx(styles.contentItem)}>
                                <div
                                    // className={clsx(styles.cbb)
                                    className={clsx(styles.formSelect)}
                                >
                                    <label htmlFor="">Category</label>
                                    <Select
                                        onChange={handleSelectChange}
                                        getOptionLabel={(x) => x.name}
                                        getOptionValue={(x) => x.id}
                                        options={options}
                                        styles={{
                                            control: (baseStyles, state) => ({
                                                ...baseStyles,
                                                borderColor: state.isFocused
                                                    ? "black"
                                                    : "#e9ecee",
                                            }),
                                        }}
                                    />
                                </div>
                            </div>
                            <div className={clsx(styles.contentItem)}>
                                <div
                                    id="seachWrap"
                                    className={clsx(styles.search)}
                                >
                                    {/* <svg
                                        xmlns="http://www.w3.org/2000/svg"
                                        aria-hidden="true"
                                        role="img"
                                        className="component-iconify MuiBox-root css-1kj4kj3 iconify iconify--eva"
                                        width="1em"
                                        height="1em"
                                        viewBox="0 0 24 24"
                                    >
                                        <path
                                            fill="currentColor"
                                            d="m20.71 19.29l-3.4-3.39A7.92 7.92 0 0 0 19 11a8 8 0 1 0-8 8a7.92 7.92 0 0 0 4.9-1.69l3.39 3.4a1 1 0 0 0 1.42 0a1 1 0 0 0 0-1.42M5 11a6 6 0 1 1 6 6a6 6 0 0 1-6-6"
                                        ></path>
                                    </svg> */}
                                    <input
                                        onChange={handleSearchInputChange}
                                        id="searchInput"
                                        type="search"
                                        placeholder="Search.."
                                    />
                                </div>
                            </div>
                        </div>
                        <div className={clsx(styles.mid)}>
                            <div
                                className={clsx(
                                    styles.titleMid,
                                    "row rounded-lg"
                                )}
                            >
                                <div className="col-lg-5">Course</div>
                                <div className="col-lg-2">Create at</div>
                                <div className="col-lg-2">Price</div>
                                <div className="col-lg-1">Discount</div>
                                <div className="col-lg-2">Action</div>
                            </div>
                            <div className={clsx(styles.containerData)}>
                                {courses &&
                                    courses.map((course, index) => {
                                        const datetimeString =
                                            "2024-04-21T18:23:01.368804";
                                        const dateTime = new Date(
                                            datetimeString
                                        );

                                        const date =
                                            dateTime.toLocaleDateString(); // Lấy ngày tháng năm
                                        const time =
                                            dateTime.toLocaleTimeString();

                                        return (
                                            <div
                                                key={index}
                                                className={clsx(
                                                    styles.item,
                                                    "row rounded-lg"
                                                )}
                                            >
                                                <div
                                                    className={clsx(
                                                        styles.field,
                                                        "col-lg-5 flex "
                                                    )}
                                                >
                                                    <div
                                                        className={clsx(
                                                            styles.cssImg
                                                        )}
                                                    >
                                                        <img
                                                            src={
                                                                course.thumbnail
                                                            }
                                                            alt=""
                                                        />
                                                    </div>
                                                    <div className="overflow-hidden">
                                                        <div
                                                            className={clsx(
                                                                styles.name
                                                            )}
                                                        >
                                                            {course.title}
                                                        </div>
                                                        <div
                                                            className={clsx(
                                                                styles.categories
                                                            )}
                                                        >
                                                            {course.category &&
                                                                course.category.join(
                                                                    ", "
                                                                )}
                                                        </div>
                                                    </div>
                                                </div>
                                                <div
                                                    className={clsx(
                                                        styles.field,
                                                        "col-lg-2"
                                                    )}
                                                >
                                                    <div
                                                        className={clsx(
                                                            styles.name
                                                        )}
                                                    >
                                                        {date}
                                                        <br />
                                                        {time}
                                                    </div>
                                                </div>
                                                <div
                                                    className={clsx(
                                                        styles.field,
                                                        "col-lg-2"
                                                    )}
                                                >
                                                    <div
                                                        className={clsx(
                                                            styles.name
                                                        )}
                                                    >
                                                        {course.price === 0
                                                            ? "Free"
                                                            : `${course.price} VND`}
                                                    </div>
                                                </div>
                                                <div
                                                    className={clsx(
                                                        styles.field,
                                                        "col-lg-1"
                                                    )}
                                                >
                                                    <div
                                                        className={clsx(
                                                            styles.name
                                                        )}
                                                    >
                                                        {course.discount}%
                                                    </div>
                                                </div>
                                                <div
                                                    className={clsx(
                                                        styles.field,
                                                        "col-lg-2"
                                                    )}
                                                >
                                                    <div
                                                        className={clsx(
                                                            styles.name,
                                                            "flex gap-4"
                                                        )}
                                                    >
                                                        <Link
                                                            to={`/admin/course/view/${course.id}`}
                                                        >
                                                            <img
                                                                src={viewIcon}
                                                                alt=""
                                                            />
                                                        </Link>
                                                        <Link
                                                            to={`/admin/course/edit/${course.id}`}
                                                        >
                                                            <img
                                                                src={editIcon}
                                                                alt=""
                                                            />
                                                        </Link>
                                                        <button
                                                            onClick={() =>
                                                                handleRemoveCourse(
                                                                    course.id
                                                                )
                                                            }
                                                        >
                                                            <img
                                                                src={deleteIcon}
                                                                alt=""
                                                                className="cursor-pointer"
                                                            />
                                                        </button>
                                                    </div>
                                                </div>
                                            </div>
                                        );
                                    })}
                            </div>
                            <div className={clsx(styles.footer)}></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default ListCourse;
